package com.hoji.adapter.out.persistence

import com.hoji.domain.model.User
import com.hoji.domain.port.out.UserPort
import org.springframework.stereotype.Component

/**
 * User Persistence Adapter (Outbound Adapter)
 * UserPort 구현체 - JPA를 사용한 영속성 처리
 */
@Component
class UserPersistenceAdapter(
    private val repository: UserJpaRepository
) : UserPort {

    override fun save(user: User): User {
        return repository.save(user)
    }

    override fun findById(id: Long): User? {
        return repository.findById(id).orElse(null)
    }

    override fun findByUsername(username: String): User? {
        return repository.findByUsername(username)
    }

    override fun findByEmail(email: String): User? {
        return repository.findByEmail(email)
    }

    override fun findAll(): List<User> {
        return repository.findAll()
    }

    override fun deleteById(id: Long) {
        repository.deleteById(id)
    }

    override fun existsById(id: Long): Boolean {
        return repository.existsById(id)
    }
}
