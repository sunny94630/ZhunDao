package com.zhaohe.zhundao.ui.home.action;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.zhaohe.app.utils.JSONUtils;
import com.zhaohe.app.utils.NetworkUtils;
import com.zhaohe.app.utils.ProgressDialogUtils;
import com.zhaohe.app.utils.SPUtils;
import com.zhaohe.app.utils.TimeUtil;
import com.zhaohe.app.utils.ToastUtil;
import com.zhaohe.zhundao.R;
import com.zhaohe.zhundao.adapter.ActionAdapter;
import com.zhaohe.zhundao.asynctask.AsyncAction;
import com.zhaohe.zhundao.asynctask.AsyncGetUserInf;
import com.zhaohe.zhundao.asynctask.AsyncSignList;
import com.zhaohe.zhundao.asynctask.AsyncUpLoadSignupStatus;
import com.zhaohe.zhundao.bean.ActionBean;
import com.zhaohe.zhundao.bean.ToolUserBean;
import com.zhaohe.zhundao.bean.dao.MySignListupBean;
import com.zhaohe.zhundao.constant.Constant;
import com.zhaohe.zhundao.dao.MySignupListDao;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

/**
 * @Description:
 * @Author:邹苏隆
 * @Since:2016/12/12 14:33
 */
public class ActionOnFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, ActionAdapter.ActionClickListener {

    //查询所有活动
    public static final int MESSAGE_ACT_ALL = 99;
    public static final int REFRESH_COMPLETE = 98;
    public static final int MESSAGE_GET_SIGNLIST = 93;
    public static final int MESSAGE_GET_USERINF = 91;
    public static final int MESSAGE_UPLOAD_SIGNUPSTATUS = 88;
    //            单页显示的数据数目
    public static final int PAGE_SIZE = 1000;




    protected View rootView;
    private IWXAPI api;
    private ActionAdapter adapter;
    private List<ActionBean> list_act;
    private ListView lv_act;
    private Handler mHandler;
    private ImageView img_share;
    private String url;
    private SwipeRefreshLayout mSwipeLayout;
    private MySignupListDao dao;
    private TextView tv_acton_suggest;
//    0,列表为空，1列表有数据
    private int list_empty=0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = getLayoutInflater(null).inflate(R.layout.fragment_acton,
                null);
        initHandler();
        initWx();
        initView();
//        初始化活动列表
        init();
//        获取用户信息，头像 昵称 性别
        getUserInf();
//        上传本地扫码
        upload();

//        test();
    }



    private void test() {
        List<ActionBean> list = new ArrayList<ActionBean>();
        for (int i = 1; i <= 20; i++) {
            ActionBean bean = new ActionBean();
            bean.setAct_title("今天天气真不错之网球小王子争夺战" + i);
            bean.setAct_endtime("截止时间" + i);
            bean.setAct_resttime("剩余" + i + "天");
            bean.setAct_sign_income("收入：" + i + "000");
            bean.setAct_status("进行中");
            bean.setAct_sign_num("已报名" + i + "人");
            list.add(bean);
        }
        adapter.refreshData(list);
    }

    private void init() {
        if (NetworkUtils.checkNetState(getActivity())) {

        } else {
//            ToastUtil.makeText(getActivity(), R.string.app_serviceError);
            jsonconver((String) SPUtils.get(getActivity(), "act_result", ""));
        }

        Dialog dialog = ProgressDialogUtils.showProgressDialog(getActivity(), getString(R.string.progress_title), getString(R.string.progress_message));
        AsyncAction asyncActivity = new AsyncAction(getActivity(), mHandler, dialog, MESSAGE_ACT_ALL);
        asyncActivity.execute();
    }

    private void initWx() {
        api = WXAPIFactory.createWXAPI(getActivity(), Constant.APP_ID, true);
        api.registerApp(Constant.APP_ID);
    }

    private void getUserInf() {
        if((SPUtils.contains(getActivity(), "NickName"))==true){
            return;
        }
        else
        { Dialog dialog = ProgressDialogUtils.showProgressDialog(getActivity(), getString(R.string.progress_title), getString(R.string.progress_message));
            AsyncGetUserInf userInf = new AsyncGetUserInf(getActivity(), mHandler, dialog, MESSAGE_GET_USERINF);
            userInf.execute();}

    }

    private void jsonconver(String result) {
        JSONObject jsonObj = JSON.parseObject(result);
        JSONArray jsonArray = jsonObj.getJSONArray("Data");
        List<ActionBean> list = new ArrayList<ActionBean>();
        for (int i = 0; i < jsonArray.size(); i++) {
            ActionBean bean = new ActionBean();
            bean.setAct_title(jsonArray.getJSONObject(i).getString("Title"));
            String time=jsonArray.getJSONObject(i).getString("TimeStop");
//            去除json传回来的时间中的T字符
            String newtime=time.replace("T"," ");
//             去除秒
            String newtime1= newtime.substring(2,newtime.length() - 3);
            bean.setAct_endtime("报名截止："+newtime1);
            String comparetime= TimeUtil.getTimeDifference(TimeUtil.getNowTime(),newtime);
            bean.setAct_resttime("(剩余"+comparetime+")");
            time=jsonArray.getJSONObject(i).getString("TimeStart");
            newtime=time.replace("T"," ");
            newtime1= newtime.substring(2,newtime.length() - 3);
            bean.setAct_starttime("活动开始："+newtime1);
            comparetime= TimeUtil.getTimeDifference(TimeUtil.getNowTime(),newtime);
            bean.setAct_resttime2("(剩余"+comparetime+")");
            bean.setAct_sign_num("已报名  " + jsonArray.getJSONObject(i).getString("HasJoinNum"));
            bean.setAct_sign_income("收入：" + jsonArray.getJSONObject(i).getString("Amount"));
            bean.setAct_status("  报名中");
            bean.setAct_content(jsonArray.getJSONObject(i).getString("Content"));
            bean.setUrl(jsonArray.getJSONObject(i).getString("ShareImgurl"));
            bean.setAct_id(jsonArray.getJSONObject(i).getString("ID"));
////            获取活动报名人数不为0的活动名单
//            if (jsonArray.getJSONObject(i).getString("HasJoinNum")!="0") {
////            获取报名列表活动ID
//                String mParam = "ActivityID=" + bean.getAct_id();
//                getSignupList(mParam);
//            }
            if (jsonArray.getJSONObject(i).getString("Status") == "0") {
                list.add(bean);
            } else {

            }
        }
       showSuggest(list);
        adapter.refreshData(list);
    }


    private void showSuggest( List<ActionBean> list){
        if(list.size()==0)
        {
            lv_act.setVisibility(GONE);
            tv_acton_suggest.setVisibility(View.VISIBLE);
        }
        else{
            lv_act.setVisibility(View.VISIBLE);
            tv_acton_suggest.setVisibility(GONE);
        }

    }

    private void getSignList(String act_id) {
        Dialog dialog = ProgressDialogUtils.showProgressDialog(getActivity(), getString(R.string.progress_title), getString(R.string.progress_message));
        AsyncSignList asyncSignList = new AsyncSignList(getActivity(), mHandler, dialog, MESSAGE_GET_SIGNLIST, act_id);
        asyncSignList.execute();
    }

    private void wxShare(int flag, ActionBean bean) {
        String actid = bean.getAct_id();
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = Constant.Url.ShareUrl + actid;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = bean.getAct_title();
        msg.description = "欢迎参加 " + bean.getAct_title();
        //这里替换一张自己工程里的图片资源
        Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.mipmap.logo);
        msg.setThumbImage(thumb);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = flag == 0 ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
        api.sendReq(req);
    }

    private void gotoSignList(String result) {
        Intent intent = new
                Intent(getActivity(), SignListActivity.class);
        JSONObject jsonObj = JSON.parseObject(result);
        if (result == null) {
            return;
        }
        if (jsonObj.getByte("Count") == 0) {
            ToastUtil.makeText(getActivity(), "暂无人报名");
            return;
        } else {
            JSONArray jsonArray = jsonObj.getJSONArray("Data");
            String act_id = jsonArray.getJSONObject(0).getString("ActivityID");
            SPUtils.put(getActivity(), "listup_" + act_id, result);
            //在Intent对象当中添加一个键值对
            intent.putExtra("act_id", act_id);
//            String result2=jsonArray.getJSONObject(0).toJSONString();
//            JSONObject jsonObject2=JSON.parseObject(jsonArray.getJSONObject(0).getString("ExtraInfo"));
//            for (Map.Entry<String, Object> entry : jsonObject2.entrySet()) {
//                System.out.println(entry.getKey() + ":" + entry.getValue());
//            }
//            String result3=jsonObject2.toString();
            startActivity(intent);
        }
    }

    private void initHandler() {
        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_ACT_ALL:
                        String result = (String) msg.obj;

                        System.out.println("Activity result:  " + result);
                        //活动列表结果
                        if (NetworkUtils.checkNetState(getActivity())) {
                            SPUtils.put(getActivity(), "act_result", result);
                            jsonconver((String) SPUtils.get(getActivity(), "act_result", ""));
                        }

                        break;
                    case REFRESH_COMPLETE:
                        mSwipeLayout.setRefreshing(false);
                        break;
                    case MESSAGE_GET_SIGNLIST:
                        String result2 = (String) msg.obj;
                        System.out.println("SignupList result:  " + result2);
                        if (NetworkUtils.checkNetState(getActivity())) {
                        }
                        gotoSignList(result2);
                        break;
                    case MESSAGE_GET_USERINF:
                        String result3 = (String) msg.obj;
                        SPUtils.put(getActivity(),"UserInfo",result3);
                        System.out.println("use"+result3);
                        savaUserInf(result3);
                        break;
                    case MESSAGE_UPLOAD_SIGNUPSTATUS:
                        String result4=(String) msg.obj;
                        JSONObject jsonObj = JSON.parseObject(result4);
                        if(jsonObj.getString("Res")=="0"){
                            changeStatus();
                            ToastUtil.makeText(getActivity(),"数据上传成功");
                        }
                        break;


                    default:
                        break;
                }
            }
        };
    }

    private void savaUserInf(String result) {

        ToolUserBean bean= JSONUtils.parseObject(result,ToolUserBean.class);
        SPUtils.put(getActivity(),"NickName", bean.getData().getNickName());
        SPUtils.put(getActivity(),"HeadImgurl",bean.getData().getHeadImgurl());
        SPUtils.put(getActivity(),"Sex",bean.getData().getSex());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 在使用这个view之前首先判断其是否存在parent view，这调用getParent()方法可以实现。
        // 如果存在parent view，那么就调用removeAllViewsInLayout()方法
        Log.i("test", "AFragment-->onCreateView");
        ViewGroup perentView = (ViewGroup) rootView.getParent();
        if (perentView != null) {
            perentView.removeAllViewsInLayout();
        }
        return rootView;
    }

    public void initView() {
//        btn_test= (Button) rootView.findViewById(btn_test);
//        btn_test.setOnClickListener(this);
        lv_act = (ListView) rootView.findViewById(R.id.lv_act);
        adapter = new ActionAdapter(getActivity());
        adapter.setActionClickListener(this);
        lv_act.setAdapter(adapter);
        mSwipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_ly);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        dao=new MySignupListDao(getActivity());
        tv_acton_suggest= (TextView) rootView.findViewById(R.id.tv_acton_suggest);

    }
    private void upload() {
        if (NetworkUtils.checkNetState(getActivity())){
        List<MySignListupBean> list=dao.queryUpdateStatus();
        String jsonString = JSON.toJSONString(list);
            if(list.size()==0){
                return;
            }
//        System.out.println(jsonString);
//ToastUtil.makeText(getActivity(),jsonString);
//        还要上传数据，现在获得了数据
            AsyncUpLoadSignupStatus async = new AsyncUpLoadSignupStatus(getActivity(), mHandler, MESSAGE_UPLOAD_SIGNUPSTATUS,jsonString);
            async.execute();
        }
    }
//    上传成功后 修改已上传数据更新状态
    private void changeStatus(){
        List<MySignListupBean> list=dao.queryUpdateStatus();
        for(int i=0;i<list.size();i++){
            MySignListupBean bean2=(MySignListupBean)list.get(i);
            MySignListupBean bean=new MySignListupBean();
            bean.setVCode(bean2.getVCode());
            bean.setStatus("true");
            bean.setUpdateStatus("false");
            bean.setCheckInID(bean2.getCheckInID());
            dao.update(bean);
        }

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
//            case btn_test:
//                IntentUtils.startActivity(getActivity(), LoginActivity.class);
//                break;
            default:
                break;
        }


    }

    @Override
    public void onRefresh() {
        if (NetworkUtils.checkNetState(getActivity())) {
            init();
            upload();
            System.out.print("成功刷新");
            mHandler.sendEmptyMessageDelayed(REFRESH_COMPLETE, 2000);
        } else {ToastUtil.makeText(getActivity(), "请联网后再试");
            mHandler.sendEmptyMessageDelayed(REFRESH_COMPLETE, 2000);
        }

    }

    @Override
    public void onEditClick(ActionBean bean) {
        Intent intent = new Intent(getActivity(), EditActWebActivity.class);

//        Intent intent = new Intent(getActivity(), EditActActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("act_id", bean.getAct_id());
        intent.putExtras(bundle);
        getActivity().startActivity(intent);
    }

    @Override
    public void onListClick(ActionBean bean) {
//       这里开始写 判断网络状况
        if (NetworkUtils.checkNetState(getActivity())) {
            String mParam = "ActivityID=" + bean.getAct_id()+"&pageSize="+PAGE_SIZE;
            getSignList(mParam);
        } else if (SPUtils.contains(getActivity(), "listup_" + bean.getAct_id()) == true) {
            Intent intent = new
                    Intent(getActivity(), SignListActivity.class);
            //在Intent对象当中添加一个键值对
            intent.putExtra("act_id", bean.getAct_id());
            startActivity(intent);

        } else {
            ToastUtil.makeText(getActivity(), "请联网后再试");
            return;
        }

    }

    @Override
    public void onShareClick(final ActionBean bean) {
        new AlertDialog.Builder(getActivity())
                .setIcon(R.mipmap.ic_launcher)
                .setTitle("活动分享")
                .setMessage("分享到")
                .setPositiveButton("朋友圈", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AlertDialog.Builder(getActivity())
                                .show();
                        wxShare(1, bean);

                    }
                })
                .setNegativeButton("微信好友", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AlertDialog.Builder(getActivity())
                                .show();
                        wxShare(0, bean);

                    }
                })
                .setNeutralButton("退出", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                    }
                })
                // 设置dialog是否为模态，false表示模态，true表示非模态
                .setCancelable(true)
                .show();
//        0分享到好友，1分享到朋友圈
//        wxShare(1, bean);
//        wxShare(0);
    }

    @Override
    public void onMoreClick(ActionBean bean) {
        ToastUtil.makeText(getActivity(),"开发中，敬请期待！");


    }

    @Override
    public void onDetailsClick(ActionBean bean) {
        Intent intent = new Intent(getActivity(), ActionDetailsActivity.class);

//        Intent intent = new Intent(getActivity(), EditActActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("act_id", bean.getAct_id());
        bundle.putString("act_title",bean.getAct_title());
        intent.putExtras(bundle);

        getActivity().startActivity(intent);
    }

}

