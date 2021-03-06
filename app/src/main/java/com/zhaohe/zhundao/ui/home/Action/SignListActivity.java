package com.zhaohe.zhundao.ui.home.action;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhaohe.app.utils.SPUtils;
import com.zhaohe.zhundao.R;
import com.zhaohe.zhundao.adapter.SignListAdapter;
import com.zhaohe.zhundao.bean.SignListBean;
import com.zhaohe.zhundao.dao.MySignListDao;
import com.zhaohe.zhundao.ui.ToolBarActivity;
import com.zhaohe.zhundao.ui.ToolBarHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author:邹苏隆
 * @Since:2016/12/14 10:52
 */
public class SignListActivity extends ToolBarActivity implements AdapterView.OnItemClickListener{
    private SignListAdapter adapter;
    private List<SignListBean> list_act;
    private ListView lv_signlist;
    private String signup_list;
    private String act_id;
    private JSONObject jsonObj;
    private JSONArray jsonArray;
    private int count=0;
    private MySignListDao dao;
    private EditText et_signlist_search;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_list);
        initToolBar("报名用户信息", R.layout.activity_sign_list);
        initView();
        init();
//        test();
    }

    private void initToolBar(String text, int layoutResID) {
        ToolBarHelper mToolBarHelper;
        mToolBarHelper = new ToolBarHelper(this, layoutResID);
        mToolBarHelper.setTvTitle(text);
        super.setTitle("");
        setContentView(mToolBarHelper.getContentView());
        toolbar = mToolBarHelper.getToolBar();
  /*把 toolbar 设置到Activity 中*/
        setSupportActionBar(toolbar);
    }


    public void init() {

        Intent intent = getIntent();
        //从Intent当中根据key取得value
        if (intent != null) {
            act_id = intent.getStringExtra("act_id");
            signup_list = (String) SPUtils.get(this, "listup_" + act_id, "");
        }
        jsonObj = JSON.parseObject(signup_list);
        jsonArray = jsonObj.getJSONArray("Data");
        int id = jsonArray.size()+1;

        List<SignListBean> list = new ArrayList<SignListBean>();
        for (int i = 0; i < jsonArray.size(); i++) {
            SignListBean bean = new SignListBean();
            id=id-1;
            bean.setSign_list_id("" + id);
            bean.setSign_list_name(jsonArray.getJSONObject(i).getString("UserName"));
            bean.setSign_list_time(jsonArray.getJSONObject(i).getString("AddTime"));
            bean.setSign_list_phone(jsonArray.getJSONObject(i).getString("Mobile"));
            bean.setNickname(jsonArray.getJSONObject(i).getString("NickName"));
            bean.setmIndex(i);
            bean.setAct_id(act_id);
//          Status  -1取消报名，0报名成功，1待缴费
            if (jsonArray.getJSONObject(i).getString("Status") == "0") {
                bean.setSign_list_status("报名成功");
            }
            if (jsonArray.getJSONObject(i).getString("Status") == "1") {
                bean.setSign_list_status("待缴费");
            }
            if (jsonArray.getJSONObject(i).getString("Status") == "-1") {
                bean.setSign_list_status("取消报名");
            }
            if (jsonArray.getJSONObject(i).getString("Status") == "2") {
                bean.setSign_list_status("待审核");
            }
            if (jsonArray.getJSONObject(i).getString("Status") == "3") {
                bean.setSign_list_status("审核失败");
            }


            list.add(bean);
        }
//        count=jsonArray.size()-1;
        dao.save(list);
        list_act=dao.queryListActID(act_id);

        adapter.refreshData(list_act);


    }

    private void test() {
        List<SignListBean> list = new ArrayList<SignListBean>();
        for (int i = 1; i <= 20; i++) {
            SignListBean bean = new SignListBean();
            bean.setSign_list_id("" + i);
            bean.setSign_list_name("小明" + i);
            bean.setSign_list_phone("1896666661" + i);
            bean.setSign_list_time("2001" + i);
            bean.setSign_list_status("报名成功");
            list.add(bean);
        }
        adapter.refreshData(list);
    }

    private void initView() {
        dao = new MySignListDao(this);

        lv_signlist = (ListView) findViewById(R.id.lv_signlist);
        adapter = new SignListAdapter(this);
        lv_signlist.setAdapter(adapter);
        lv_signlist.setOnItemClickListener(this);
        et_signlist_search= (EditText) findViewById(R.id.et_signlist_search);
et_signlist_search.addTextChangedListener(new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //TODO:
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        list_act=  dao.queryListActIDAndPhoneOrName(act_id,et_signlist_search.getText().toString());
        adapter.refreshData(list_act);
    }

    @Override
    public void afterTextChanged(Editable s) {
        //TODO:
    }
});
    }

        @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
      SignListBean bean= adapter.getItem(i);
      int m=  bean.getmIndex();
        String name = jsonArray.getJSONObject(m).getString("UserName");
        String phone = jsonArray.getJSONObject(m).getString("Mobile");
        String unit = jsonArray.getJSONObject(i).getString("Company");
        String sex = jsonArray.getJSONObject(i).getString("Sex");
        String dep = jsonArray.getJSONObject(i).getString("Depart");
        String industry = jsonArray.getJSONObject(i).getString("Industry");
        String duty = jsonArray.getJSONObject(i).getString("Duty");
        String id_card = jsonArray.getJSONObject(i).getString("IDcard");
        String email = jsonArray.getJSONObject(i).getString("Email");
        String join_num = jsonArray.getJSONObject(i).getString("Num");
        String add = jsonArray.getJSONObject(i).getString("Address");
        String remark = jsonArray.getJSONObject(i).getString("Remark");
        String amount = jsonArray.getJSONObject(i).getString("Amount");
        String title = jsonArray.getJSONObject(i).getString("Title");
        Intent intent = new
                Intent(this, SignListUserActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("phone", phone);
        intent.putExtra("unit", unit);
        intent.putExtra("sex", sex);
        intent.putExtra("dep", dep);
        intent.putExtra("industry", industry);
        intent.putExtra("duty", duty);
        intent.putExtra("id_card", id_card);
        intent.putExtra("email", email);
        intent.putExtra("join_num", join_num);
        intent.putExtra("add", add);
        intent.putExtra("remark", remark);
        intent.putExtra("amount", amount);
        intent.putExtra("title", title);
        intent.putExtra("act_id", act_id);
        JSONObject jsonObject2 = null;
        if (JSON.parseObject(jsonArray.getJSONObject(i).getString("ExtraInfo")) != null) {
            jsonObject2 = JSON.parseObject(jsonArray.getJSONObject(i).getString("ExtraInfo"));
            String extra = jsonObject2.toString();
            intent.putExtra("extra", extra);
        }
        startActivity(intent);


    }
}