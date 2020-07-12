package com.example.proximitychat;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


public class MyViewModel extends ViewModel {
    private MutableLiveData<Boolean> myBool;

    public void setData(Boolean data) {
        myBool.postValue(data);
    }

    public MutableLiveData<Boolean> getData() {
        if (myBool == null) {
            myBool = new MutableLiveData<Boolean>();
        }
        return myBool;
    }
}
