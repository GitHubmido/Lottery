package lt.lottery;

import android.content.Context;

public class MessgeBox {
    public CustomDialog.Builder builder = null;
    public MessgeBox(Context context,String title,String msgInfo){
        builder = new CustomDialog.Builder(context);
        builder.setMessage(msgInfo);
        builder.setTitle(title);
    }
    public interface alertMsgInterface{
        void setOnclickButton(CustomDialog.Builder builder);
    }
    public void show(alertMsgInterface alertMsgInterface){
        alertMsgInterface.setOnclickButton(this.builder);
        this.builder.create().show();
    }
}
