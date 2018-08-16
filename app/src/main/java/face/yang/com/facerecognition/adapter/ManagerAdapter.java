package face.yang.com.facerecognition.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.aip.db.DBManager;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

import face.yang.com.facerecognition.R;
import face.yang.com.facerecognition.entity.UserEntity;
import face.yang.com.facerecognition.utils.FaceUtils;

public class ManagerAdapter extends BaseAdapter {

    private final List<UserEntity> data;
    private final Context context;
    private boolean delete=false;

    public ManagerAdapter(Context context,List<UserEntity> data) {
        this.data=data;
        this.context=context;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder holder;
        if(view==null){

            holder = new ViewHolder();
            view = View.inflate(context, R.layout.item_manager, null);
            holder.imageView=view.findViewById(R.id.image_head);
            holder.naemView=view.findViewById(R.id.manager_name);
            holder.uidView=view.findViewById(R.id.manager_uid);
            holder.checkBox=view.findViewById(R.id.checkbox_);
            holder.relativeLayout=view.findViewById(R.id.relativelayout_);

            view.setTag(holder);
        }else {
            holder= (ViewHolder) view.getTag();
        }

        RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        if(delete){
            layoutParams.leftMargin= FaceUtils.dp2px(context,60);
            holder.checkBox.setVisibility(View.VISIBLE);
        }else {
            layoutParams.leftMargin= FaceUtils.dp2px(context,0);
            holder.checkBox.setVisibility(View.GONE);
        }

        holder.relativeLayout.setLayoutParams(layoutParams);

        final UserEntity userEntity = data.get(i);


        holder.uidView.setText("UID编号: "+userEntity.getUid());
        holder.naemView.setText(userEntity.getName());
        if(userEntity.getImagePath()!=null){
            Glide.with(context).load(new File(userEntity.getImagePath())).into(holder.imageView);

        }

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                data.remove(userEntity);
                notifyDataSetChanged();
                DBManager.getInstance().deleteUser(userEntity.getUid(),userEntity.getType());
            }
        });
        return view;
    }

    static class ViewHolder {
        ImageView imageView;
        TextView naemView;
        TextView uidView;
        TextView checkBox;
        RelativeLayout relativeLayout;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }
}
