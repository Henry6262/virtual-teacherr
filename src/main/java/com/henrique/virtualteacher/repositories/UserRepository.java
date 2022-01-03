package com.henrique.virtualteacher.repositories;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findById(int id);

    Optional<User> findByEmail(String email);

    List<User> findAllByEnabled(boolean enabled);

}
