package com.ejemplo.ecoreport.core.data

import com.ejemplo.ecoreport.core.model.Incidence
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class IncidenceRepository {
    private val databaseUrl = "https://ecoreport-c201d-default-rtdb.firebaseio.com/"
    private val dbRef = FirebaseDatabase.getInstance(databaseUrl).getReference("incidencias")

    // Escuchar lista reactiva en tiempo real (para TV y Móvil)
    fun getIncidencesFlow(): Flow<List<Incidence>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Incidence>()
                for (child in snapshot.children) {
                    val item = child.getValue(Incidence::class.java)
                    if (item != null) list.add(item)
                }
                // Ordenar por fecha reciente
                trySend(list.sortedByDescending { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        dbRef.addValueEventListener(listener)
        awaitClose { dbRef.removeEventListener(listener) }
    }

    // Guardar o actualizar reporte desde el Móvil
    fun saveIncidence(incidence: Incidence, onComplete: (Boolean) -> Unit) {
        val key = if (incidence.id.isEmpty()) dbRef.push().key ?: "" else incidence.id
        val itemToSave = incidence.copy(id = key)
        dbRef.child(key).setValue(itemToSave).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }
}