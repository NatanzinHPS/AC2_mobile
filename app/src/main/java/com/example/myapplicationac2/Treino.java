package com.example.myapplicationac2;

public class Treino {
    private String id;
    private String nomeTreino;
    private String tipoTreino;
    private String dataTreino;
    private int duracaoTreino;
    private String intensidadeTreino;
    private boolean foiConcluido;

    public Treino(){

    }
    public Treino(String id, String nomeTreino, boolean foiConcluido, String intensidadeTreino, int duracaoTreino, String dataTreino, String tipoTreino) {
        this.id = id;
        this.nomeTreino = nomeTreino;
        this.foiConcluido = foiConcluido;
        this.intensidadeTreino = intensidadeTreino;
        this.duracaoTreino = duracaoTreino;
        this.dataTreino = dataTreino;
        this.tipoTreino = tipoTreino;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNomeTreino() {
        return nomeTreino;
    }

    public void setNomeTreino(String nomeTreino) {
        this.nomeTreino = nomeTreino;
    }

    public String getTipoTreino() {
        return tipoTreino;
    }

    public void setTipoTreino(String tipoTreino) {
        this.tipoTreino = tipoTreino;
    }

    public String getDataTreino() {
        return dataTreino;
    }

    public void setDataTreino(String dataTreino) {
        this.dataTreino = dataTreino;
    }

    public int getDuracaoTreino() {
        return duracaoTreino;
    }

    public void setDuracaoTreino(int duracaoTreino) {
        this.duracaoTreino = duracaoTreino;
    }

    public String getIntensidadeTreino() {
        return intensidadeTreino;
    }

    public void setIntensidadeTreino(String intensidadeTreino) {
        this.intensidadeTreino = intensidadeTreino;
    }

    public boolean isFoiConcluido() {
        return foiConcluido;
    }

    public void setFoiConcluido(boolean foiConcluido) {
        this.foiConcluido = foiConcluido;
    }


}
