
package com.smartcampushelpdesk.helpdesk_api.repositories;

import com.smartcampushelpdesk.helpdesk_api.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByCollegeId(UUID collegeId);

    Optional<Category> findByIdAndCollegeId(
        UUID id,
        UUID collegeId
    );
}
