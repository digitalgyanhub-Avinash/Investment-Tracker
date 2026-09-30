package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Committee
import com.example.data.model.Holding
import com.example.data.model.Ipo
import com.example.data.model.Sip
import kotlinx.coroutines.flow.Flow

@Dao
interface InvestmentDao {
    // Holdings
    @Query("SELECT * FROM holdings ORDER BY updatedAt DESC")
    fun getAllHoldings(): Flow<List<Holding>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolding(holding: Holding)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoldings(holdings: List<Holding>)

    @Update
    suspend fun updateHolding(holding: Holding)

    @Delete
    suspend fun deleteHolding(holding: Holding)

    @Query("DELETE FROM holdings WHERE id = :id")
    suspend fun deleteHoldingById(id: String): Int

    @Query("DELETE FROM holdings")
    suspend fun clearHoldings(): Int

    // SIPs
    @Query("SELECT * FROM sips ORDER BY updatedAt DESC")
    fun getAllSips(): Flow<List<Sip>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSip(sip: Sip)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSips(sips: List<Sip>)

    @Update
    suspend fun updateSip(sip: Sip)

    @Delete
    suspend fun deleteSip(sip: Sip)

    @Query("DELETE FROM sips WHERE id = :id")
    suspend fun deleteSipById(id: String): Int

    @Query("DELETE FROM sips")
    suspend fun clearSips(): Int

    // IPOs
    @Query("SELECT * FROM ipos ORDER BY updatedAt DESC")
    fun getAllIpos(): Flow<List<Ipo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIpo(ipo: Ipo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIpos(ipos: List<Ipo>)

    @Update
    suspend fun updateIpo(ipo: Ipo)

    @Delete
    suspend fun deleteIpo(ipo: Ipo)

    @Query("DELETE FROM ipos WHERE id = :id")
    suspend fun deleteIpoById(id: String): Int

    @Query("DELETE FROM ipos")
    suspend fun clearIpos(): Int

    // Committees
    @Query("SELECT * FROM committees ORDER BY updatedAt DESC")
    fun getAllCommittees(): Flow<List<Committee>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommittee(committee: Committee)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommittees(committees: List<Committee>)

    @Update
    suspend fun updateCommittee(committee: Committee)

    @Delete
    suspend fun deleteCommittee(committee: Committee)

    @Query("DELETE FROM committees WHERE id = :id")
    suspend fun deleteCommitteeById(id: String): Int

    @Query("DELETE FROM committees")
    suspend fun clearCommittees(): Int
}
