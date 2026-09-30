package com.example.data.repository

import com.example.data.local.InvestmentDao
import com.example.data.model.Committee
import com.example.data.model.Holding
import com.example.data.model.Ipo
import com.example.data.model.Sip
import kotlinx.coroutines.flow.Flow

class InvestmentRepository(private val dao: InvestmentDao) {

    val allHoldings: Flow<List<Holding>> = dao.getAllHoldings()
    val allSips: Flow<List<Sip>> = dao.getAllSips()
    val allIpos: Flow<List<Ipo>> = dao.getAllIpos()
    val allCommittees: Flow<List<Committee>> = dao.getAllCommittees()

    // Holdings CRUD
    suspend fun insertHolding(holding: Holding) = dao.insertHolding(holding)
    suspend fun insertHoldings(holdings: List<Holding>) = dao.insertHoldings(holdings)
    suspend fun updateHolding(holding: Holding) = dao.updateHolding(holding)
    suspend fun deleteHolding(holding: Holding) = dao.deleteHolding(holding)
    suspend fun deleteHoldingById(id: String) = dao.deleteHoldingById(id)
    suspend fun clearHoldings() = dao.clearHoldings()

    // SIPs CRUD
    suspend fun insertSip(sip: Sip) = dao.insertSip(sip)
    suspend fun insertSips(sips: List<Sip>) = dao.insertSips(sips)
    suspend fun updateSip(sip: Sip) = dao.updateSip(sip)
    suspend fun deleteSip(sip: Sip) = dao.deleteSip(sip)
    suspend fun deleteSipById(id: String) = dao.deleteSipById(id)
    suspend fun clearSips() = dao.clearSips()

    // IPOs CRUD
    suspend fun insertIpo(ipo: Ipo) = dao.insertIpo(ipo)
    suspend fun insertIpos(ipos: List<Ipo>) = dao.insertIpos(ipos)
    suspend fun updateIpo(ipo: Ipo) = dao.updateIpo(ipo)
    suspend fun deleteIpo(ipo: Ipo) = dao.deleteIpo(ipo)
    suspend fun deleteIpoById(id: String) = dao.deleteIpoById(id)
    suspend fun clearIpos() = dao.clearIpos()

    // Committees CRUD
    suspend fun insertCommittee(committee: Committee) = dao.insertCommittee(committee)
    suspend fun insertCommittees(committees: List<Committee>) = dao.insertCommittees(committees)
    suspend fun updateCommittee(committee: Committee) = dao.updateCommittee(committee)
    suspend fun deleteCommittee(committee: Committee) = dao.deleteCommittee(committee)
    suspend fun deleteCommitteeById(id: String) = dao.deleteCommitteeById(id)
    suspend fun clearCommittees() = dao.clearCommittees()
}
