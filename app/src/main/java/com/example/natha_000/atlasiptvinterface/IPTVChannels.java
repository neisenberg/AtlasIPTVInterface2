package com.example.natha_000.atlasiptvinterface;

/**
 * Created by natha_000 on 10/25/2014.
 */


import java.util.ArrayList;

public class IPTVChannels {
    //private variables
    int _id;
    String _name;
    String _url;

    // Empty constructor
    public IPTVChannels(){
    }

    // constructor
    public IPTVChannels(int id, String name, String _url){
        this._id = id;
        this._name = name;
        this._url = _url;
    }

    // constructor
    public IPTVChannels(String name, String _url){
        this._name = name;
        this._url = _url;
    }

    public void setID(int id){
        this._id = id;
    }

    public void setName(String name){
        this._name = name;
    }

    public void setUrl(String url){
        this._url = url;
    }

    public void setAll(int id, String name, String url) {
        this._id = id;
        this._name = name;
        this._url = url;
    }

    public int getID(){
        return this._id;
    }

    public String getName(){
        return this._name;
    }

    public String getUrl(){
        return this._url;
    }

}