package com.arekalov.blps.repository

import com.arekalov.blps.model.Skill
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SkillRepository : JpaRepository<Skill, UUID> {
    fun findByName(name: String): Skill?
    fun findByNameIn(names: List<String>): List<Skill>
}
