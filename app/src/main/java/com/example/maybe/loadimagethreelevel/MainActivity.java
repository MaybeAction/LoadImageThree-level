package com.example.maybe.loadimagethreelevel;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView iv;
    private Button btnLoadImage;
    private String url="http://img15.3lian.com/2015/f2/160/d/65.jpg";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv= (ImageView) findViewById(R.id.iv);
        btnLoadImage= (Button) findViewById(R.id.btnLoadImage);
        btnLoadImage.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        Toast.makeText(this, "正在下载", Toast.LENGTH_SHORT).show();
        //调用三级缓存下载
        LoadImage.getLoadImage(MainActivity.this,url,iv,500,500);

    }
}
