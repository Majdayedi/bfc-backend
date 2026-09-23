package bfc.bfc.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "client_logos")
public class ClientLogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "logo_url", nullable = false)
    private String logoUrl;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    public ClientLogo() {}

    public ClientLogo(String name, String logoUrl, Integer displayOrder) {
        this.name = name;
        this.logoUrl = logoUrl;
        this.displayOrder = displayOrder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
