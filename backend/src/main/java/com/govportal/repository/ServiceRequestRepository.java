package com.govportal.repository;

import com.govportal.domain.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, String> {

    List<ServiceRequest> findByCitizenSubOrderByCreatedAtDesc(String citizenSub);

    List<ServiceRequest> findAllByOrderByCreatedAtDesc();
}
