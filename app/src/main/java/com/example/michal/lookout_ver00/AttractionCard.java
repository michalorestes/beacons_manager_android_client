package com.example.michal.lookout_ver00;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.cert.TrustAnchor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.jar.Manifest;

import static com.example.michal.lookout_ver00.R.id.textView;

/**
 * Created by Michal on 26/03/2017.
 */

public class AttractionCard extends CardView  {

    TextView title;
    ImageView image;
    public AttractionCard(Context context){
        //set up card and main layout
        super(context);


        this.setMinimumHeight(950);
        this.setCardBackgroundColor(Color.GREEN);
        LinearLayout.LayoutParams cardParams= new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 650);
        cardParams.setMargins(0, 20, 0, 20);
        this.setLayoutParams(cardParams);



        RelativeLayout mainLayout = new RelativeLayout(context);
        mainLayout.setBackgroundColor(Color.YELLOW);
        RelativeLayout.LayoutParams layoutParams= new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 950);
        mainLayout.setLayoutParams(layoutParams);
       //s mainLayout.setOrientation(LinearLayout.VERTICAL);

        //set up image container
        image = new ImageView(context);
        image.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 700));
        image.setBackgroundColor(Color.LTGRAY);


        //set up title
        title = new TextView(context);
        title.setText("TITLE");
        title.setTextSize(26);
        title.setBackgroundColor(Color.parseColor("#BFFFFFFF"));
        //Align title to the bottom of image
        RelativeLayout.LayoutParams params= new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, image.getId());
        params.addRule(RelativeLayout.ALIGN_BASELINE, image.getId());

        params.bottomMargin = 300;
        title.setTextColor(Color.parseColor("#FF424242"));
        title.setPadding(50, 0, 0 ,0);
        title.setLayoutParams(params);


        //save button button
        Button btn_save = new Button(context);
        btn_save.setText("my tex");
        RelativeLayout.LayoutParams param2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        param2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, mainLayout.getId());
        param2.addRule(RelativeLayout.ALIGN_PARENT_END, mainLayout.getId());
        param2.bottomMargin = 25;
        param2.rightMargin = 25;
        btn_save.setLayoutParams(param2);

        mainLayout.addView(image);
        mainLayout.addView(title);

        this.addView(mainLayout);


    }

    public void setTitle(String t){
        title.setText(t);
    }


    public void setImage(Bitmap bm){
        Drawable d = new BitmapDrawable(bm);
        image.setBackground(d);

    }


}
