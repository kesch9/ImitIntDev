package ru.model;

import javax.persistence.*;

@Entity
@Table (name = "CKC")
public class CKCBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ckc_id")
    private Long ckcId;
    @Column(name = "adres")
    private Double adres;
    @Column(name = "name")
    private String name;
    @Column(name = "val")
    private Double value;
    @Column(name = "description")
    private String description;
    @ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.LAZY, targetEntity = Model.class)
    @JoinColumn(name = "model_id")
    private Model model;

    public CKCBase(Long ckcId, Double adres, String name, Double value, String description) {
        this.ckcId = ckcId;
        this.adres = adres;
        this.name = name;
        this.value = value;
        this.description = description;
    }

    public CKCBase(String name, Double adres, Double value, String description) {
        this.adres = adres;
        this.name = name;
        this.value = value;
        this.description = description;
    }

    public Long getCkcId() {
        return ckcId;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public void setCkcId(Long ckcId) {
        this.ckcId = ckcId;
    }

    public Double getAdres() {
        return adres;
    }

    public void setAdres(Double adres) {
        this.adres = adres;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
