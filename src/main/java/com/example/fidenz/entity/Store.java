package com.example.fidenz.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "stores")
public class Store {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String name;
    
    @NotBlank
    @Size(max = 200)
    @Column(nullable = false)
    private String address;
    
    @NotBlank
    @Size(max = 20)
    @Column(nullable = false)
    private String phone;
    
    @Size(max = 100)
    private String managerName;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Inventory> inventories;
    
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SalesTransaction> salesTransactions;
    
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReorderRecommendation> reorderRecommendations;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


    public Store() {}

    public Store(String name, String address, String phone, String managerName) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.managerName = managerName;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Inventory> getInventories() { return inventories; }
    public void setInventories(List<Inventory> inventories) { this.inventories = inventories; }

    public List<SalesTransaction> getSalesTransactions() { return salesTransactions; }
    public void setSalesTransactions(List<SalesTransaction> salesTransactions) { this.salesTransactions = salesTransactions; }

    public List<ReorderRecommendation> getReorderRecommendations() { return reorderRecommendations; }
    public void setReorderRecommendations(List<ReorderRecommendation> reorderRecommendations) { this.reorderRecommendations = reorderRecommendations; }
}
