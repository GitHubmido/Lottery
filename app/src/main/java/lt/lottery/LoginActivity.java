package lt.lottery;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private ImageView img;
    private EditText edit_code,edit_user,edit_pass;
    private String base_api = "";
    private String Cookies = "";

    //在消息队列中实现对控件的更改
    private Handler handle = new Handler() {
        public void handleMessage(Message msg) {
            /**
             * 1000 成功获取图片验证码，并进行UI显示
             * 1001 登陆发生异常，弹出系统默认提示框
             * 1002 成功返回登陆结果，弹出自定义提示框
             * */
            switch (msg.what) {
                case 1000:
                    Bitmap bmp=(Bitmap)msg.obj;
                    img.setImageBitmap(bmp);
                    img.getBackground().setAlpha(0);
                    break;
                case 1001:
                    new AlertDialog.Builder(LoginActivity.this)
                            .setMessage(msg.obj.toString()).show();
                    break;
                case 1002:
                    Map<String,String> map = (Map<String, String>)msg.obj;
                    MessgeBox messgeBox = new MessgeBox(LoginActivity.this,map.get("title"),map.get("content"));
                    messgeBox.show(new MessgeBox.alertMsgInterface() {
                        @Override
                        public void setOnclickButton(CustomDialog.Builder builder) {
                            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    Intent in = new Intent();
                                    in.setClass(LoginActivity.this,MainActivity.class);
                                    startActivity(in);
                                    LoginActivity.this.finish();
                                }
                            });
                        }
                    });
                    break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        base_api = this.getString(R.string.base_api);
        //绑定元素
        img = (ImageView) findViewById(R.id.login_code);
        edit_code = (EditText) findViewById(R.id.edit_code);
        edit_user = (EditText) findViewById(R.id.login_edit_user);
        edit_pass = (EditText) findViewById(R.id.login_edit_pass);

        //处理事件
        loadImage();    //加载验证码
        //绑定验证码点击事件
        ImageView imgCode = (ImageView)findViewById(R.id.login_code);
        imgCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadImage();    //加载验证码
            }
        });
        //绑定登陆点击按钮
        Button btn = (Button)findViewById(R.id.login_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        login_even();
                    }
                }).start();
            }
        });
    }
    private void login_even(){
        Map<String, String> map = new HashMap<String, String>();
        map.put("do","login");
        map.put("user","login");
        map.put("pass","login");
        map.put("code","login");
        String rs = HttpApi.HttpPost(base_api,map,"",Cookies);
        try {
            String reCode = "";String reInfo = "";
            JSONTokener jsonParser = new JSONTokener(rs);
            JSONObject person = (JSONObject) jsonParser.nextValue();
            reCode = person.getString("reCode");
            reInfo = person.getString("reInfo");
            if(reCode.equals("1000")){
                //登陆成功
                JSONObject reData = person.getJSONObject("reData");
                String username = reData.getString("username");
                Double coin = reData.getDouble("coin");
                Message msg = new Message();
                msg.what = 1002;
                Map<String,String> msgMap = new HashMap<String, String>();
                msgMap.put("title","登陆成功");
                msgMap.put("content","欢迎您回来，亲爱的" + username + "。" + System.getProperty("line.separator") + "您的当前余额为：" + coin.toString() + System.getProperty("line.separator") + "祝您游戏愉快！");
                msg.obj = msgMap;
                handle.sendMessage(msg);
            }else{
                //登陆失败
                Message msg = new Message();
                msg.what = 1002;
                Map<String,String> msgMap = new HashMap<String, String>();
                msgMap.put("title","登陆失败");
                msgMap.put("content",reInfo);
                msg.obj = msgMap;
                handle.sendMessage(msg);
            }
        }catch (Exception e) {
            Message msg = new Message();
            msg.what = 1001;
            msg.obj = "登陆异常";
            handle.sendMessage(msg);
        }
    }
    //加载图片信息
    private void loadImage() {
        //新建线程加载图片信息，发送到消息队列中
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                Bitmap bmp = getURLimage(base_api + "?do=getCode");
                Message msg = new Message();
                msg.what = 1000;
                msg.obj = bmp;
                handle.sendMessage(msg);
            }
        }).start();
    }

    //加载验证码
    public Bitmap getURLimage(String url) {
        Bitmap bmp = null;
        try {
            URL myurl = new URL(url);
            // 获得连接
            HttpURLConnection conn = (HttpURLConnection) myurl.openConnection();
            conn.setConnectTimeout(6000);//设置超时
            conn.setDoInput(true);
            conn.setUseCaches(false);//不缓存
            conn.connect();
            InputStream is = conn.getInputStream();//获得图片的数据流
            bmp = BitmapFactory.decodeStream(is);
            String cookie_ = conn.getHeaderField("set-cookie");  //获取Cookies
            if(cookie_!=null && cookie_.length()>0){
                Cookies = cookie_;
            }
            is.close();
        } catch (Exception e) {
            new AlertDialog.Builder(this)
                    .setMessage("获取验证码出错").show();
        }
        return bmp;
    }
}
