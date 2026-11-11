
package com.smartcampushelpdesk.helpdesk_api.repositories;

import com.smartcampushelpdesk.helpdesk_api.entities.Resource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

import java.util.UUID;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, UUID> {
    @Query(value = """
        SELECT
            r.id AS id,
            r.resource_type_id AS resource_type_id,
            r.room_id AS room_id,
            r.name AS name,
            r.code AS code,
            r.description AS description,
            rt.name AS type,
            rm.name AS room,
            b.name AS building,
            r.capacity AS capacity,
            r.is_bookable AS bookable,
            r.is_active AS active
        FROM public.resources r
        LEFT JOIN public.resource_types rt
            ON r.resource_type_id = rt.id
        LEFT JOIN public.rooms rm
            ON r.room_id = rm.id
        LEFT JOIN public.buildings b
            ON rm.building_id = b.id
        ORDER BY r.name
        """, nativeQuery = true)
    List<Object[]> findResourceDirectory();
}