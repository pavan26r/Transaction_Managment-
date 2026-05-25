package com.finance.manager.service;

import com.finance.manager.dto.request.CategoryRequest;
import com.finance.manager.dto.response.ResponseDTOs.*;
import com.finance.manager.entity.*;
import com.finance.manager.exception.BadRequestException;
import com.finance.manager.exception.ConflictException;
import com.finance.manager.exception.ForbiddenException;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.repository.CustomCategoryRepository;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.impl.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CustomCategoryRepository categoryRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private CategoryService categoryService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@test.com").build();

        Authentication auth = mock(Authentication.class);
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("test@test.com");
        SecurityContextHolder.setContext(sc);

        when(userRepository.findByUsername("test@test.com")).thenReturn(Optional.of(user));
    }

    @Test
    void getAllCategories_ReturnsDefaultAndCustom() {
        CustomCategory cc = CustomCategory.builder().id(1L).name("MyBudget")
                .type(CategoryType.EXPENSE).user(user).build();
        when(categoryRepository.findByUserId(1L)).thenReturn(List.of(cc));

        CategoryListResponse res = categoryService.getAllCategories();
        assertTrue(res.getCategories().stream().anyMatch(c -> c.getName().equals("Salary") && !c.isCustom()));
        assertTrue(res.getCategories().stream().anyMatch(c -> c.getName().equals("MyBudget") && c.isCustom()));
    }

    @Test
    void createCategory_Success() {
        CategoryRequest req = new CategoryRequest();
        req.setName("SideIncome");
        req.setType(CategoryType.INCOME);

        when(categoryRepository.existsByNameIgnoreCaseAndUserId("SideIncome", 1L)).thenReturn(false);
        CustomCategory saved = CustomCategory.builder().name("SideIncome").type(CategoryType.INCOME).user(user).build();
        when(categoryRepository.save(any())).thenReturn(saved);

        CategoryResponse res = categoryService.createCategory(req);
        assertEquals("SideIncome", res.getName());
        assertTrue(res.isCustom());
    }

    @Test
    void createCategory_ConflictsWithDefault_Throws() {
        CategoryRequest req = new CategoryRequest();
        req.setName("Salary");
        req.setType(CategoryType.INCOME);

        assertThrows(ConflictException.class, () -> categoryService.createCategory(req));
    }

    @Test
    void createCategory_Duplicate_Throws() {
        CategoryRequest req = new CategoryRequest();
        req.setName("MyBudget");
        req.setType(CategoryType.EXPENSE);

        when(categoryRepository.existsByNameIgnoreCaseAndUserId("MyBudget", 1L)).thenReturn(true);
        assertThrows(ConflictException.class, () -> categoryService.createCategory(req));
    }

    @Test
    void deleteCategory_Success() {
        CustomCategory cc = CustomCategory.builder().id(1L).name("OldCat")
                .type(CategoryType.EXPENSE).user(user).build();
        when(categoryRepository.findByNameIgnoreCaseAndUserId("OldCat", 1L)).thenReturn(Optional.of(cc));
        when(transactionRepository.existsByUserIdAndCategory(1L, "OldCat")).thenReturn(false);

        MessageResponse res = categoryService.deleteCategory("OldCat");
        assertEquals("Category deleted successfully", res.getMessage());
        verify(categoryRepository).delete(cc);
    }

    @Test
    void deleteCategory_DefaultCategory_ThrowsForbidden() {
        assertThrows(ForbiddenException.class, () -> categoryService.deleteCategory("Salary"));
    }

    @Test
    void deleteCategory_UsedByTransaction_ThrowsBadRequest() {
        CustomCategory cc = CustomCategory.builder().id(1L).name("OldCat")
                .type(CategoryType.EXPENSE).user(user).build();
        when(categoryRepository.findByNameIgnoreCaseAndUserId("OldCat", 1L)).thenReturn(Optional.of(cc));
        when(transactionRepository.existsByUserIdAndCategory(1L, "OldCat")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> categoryService.deleteCategory("OldCat"));
    }

    @Test
    void deleteCategory_NotFound_Throws() {
        when(categoryRepository.findByNameIgnoreCaseAndUserId("Unknown", 1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory("Unknown"));
    }
}
