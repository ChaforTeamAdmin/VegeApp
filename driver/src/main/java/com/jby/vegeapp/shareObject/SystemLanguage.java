package com.jby.vegeapp.shareObject;

import android.content.Context;

import com.jby.vegeapp.R;

public class SystemLanguage {
    private Context context;
    private String languageType;


    public SystemLanguage(Context context, String languageType){
        this.context = context;
        this.languageType = languageType;
    }

    public String language(int position){
        String[] languageArray = context.getResources().getStringArray(getLanguageType());
        return languageArray[position];
    }

    private int getLanguageType(){
        int language;
        switch(languageType){
            case "1":
                language = R.array.english_language;
                break;
            case "2":
                language = R.array.chinese_language;
                break;
            case"3":
                language = R.array.malay_language;
                break;
            default:
                language = R.array.english_language;
                break;
        }
        return language;
    }
}
