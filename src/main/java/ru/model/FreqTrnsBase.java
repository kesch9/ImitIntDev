package ru.model;


import javax.persistence.*;

@Entity
@Table(name = "FreqTrns")
public class FreqTrnsBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FreqTrns")
    private Long freqTrnsId;
    @Column(name = "adres")
    private int adres;
    @Column(name = "name")
    private String name;
    @Column(name = "value")
    private Integer value;
    @Column(name = "description")
    private String description;

    @ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.LAZY, targetEntity = Model.class)
    @JoinColumn(name = "model_id")
    private Model model;

    public Long getFreqTrnsId() {
        return freqTrnsId;
    }

    public void setFreqTrnsId(Long freqTrnsId) {
        this.freqTrnsId = freqTrnsId;
    }

    public int getAdres() {
        return adres;
    }

    public void setAdres(int adres) {
        this.adres = adres;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public FreqTrnsBase(Long freqTrnsId, int adres, String name, Integer value, String description, Model model) {

        this.freqTrnsId = freqTrnsId;
        this.adres = adres;
        this.name = name;
        this.value = value;
        this.description = description;
        this.model = model;
    }
}
