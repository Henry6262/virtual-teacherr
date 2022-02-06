package com.henrique.virtualteacher.services;

import com.henrique.virtualteacher.Helpers;
import com.henrique.virtualteacher.entities.Assignment;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.repositories.AssignmentRepository;
import com.henrique.virtualteacher.services.implementation.AssignmentServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AssignmentServiceTests {

    @Mock
    AssignmentRepository assignmentRepository;
    @Mock
    ModelMapper modelMapper;

    @InjectMocks
    AssignmentServiceImpl assignmentService;

    @Test
    public void getById_shouldThrowException_whenEntityNotFound() {
        Assignment mockAssignment = Helpers.createMockGradedAssignment();

        Assertions.assertThrows(EntityNotFoundException.class, () -> assignmentService.getById(mockAssignment.getId(), mockAssignment.getUser()));
    }

    @Test
    public void getById_shouldThrowException_whenUserIsNotAuthorized() {
        Assignment mockAssignment = Helpers.createMockGradedAssignment();
        User initiator = Helpers.createMockUser();
        initiator.setId(21);

        Mockito.when(assignmentRepository.findById(mockAssignment.getId())).thenReturn(Optional.of(mockAssignment));

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> assignmentService.getById(mockAssignment.getId(), initiator));
    }

    @Test
    public void getAllByUserIdAndCourseId_shouldThrowException_whenEntityNotFound() {
        Assignment mockAssignment = Helpers.createMockGradedAssignment();

        Assertions.assertThrows(EntityNotFoundException.class, () -> assignmentService.getAllByUserIdAndCourseId(mockAssignment.getUser().getId(), mockAssignment.getLecture().getCourse().getId(),mockAssignment.getUser()));
    }}
