package com.example.learnabird.model;

/*
* Executable
*
* Model class use to pass data to background activity in ListAdapter
 */
public class Executable {

    private int pos;
    private String name;

    public Executable(int pos, String name){
        this.pos = pos;
        this.name = name;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
