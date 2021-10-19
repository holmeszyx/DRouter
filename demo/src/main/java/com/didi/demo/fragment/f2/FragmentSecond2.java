package com.didi.demo.fragment.f2;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.didi.demo.interceptor.OutClass;
import com.didi.drouter.annotation.Router;
import com.didi.drouter.api.DRouter;
import com.didi.drouter.api.Extend;
import com.didi.drouter.demo.R;
import com.didi.drouter.router.RouterCallback;
import com.didi.drouter.utils.RouterLogger;

/**
 * Created by gaowei on 2018/8/31
 */
@Router(path = "/fragment/second/", priority = 10, interceptor = {OutClass.InnerInterceptor.class})
public class FragmentSecond2 extends Fragment {

    public FragmentSecond2() {
        RouterLogger.getAppLogger().d("SecondFragment2 实例化");
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_title, container, false);
        view.findViewById(R.id.click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DRouter.build("/activity/result")
                        .start(getContext(), new RouterCallback.ActivityCallback() {
                            @Override
                            public void onActivityResult(int resultCode, Intent data) {
                                if (data != null) {
                                    RouterLogger.toast(data.getStringExtra("result"));
                                }
                            }
                        });
            }
        });

        ((TextView)view.findViewById(R.id.title)).setText(getArguments().getString(Extend.REQUEST_BUILD_URI));
        return view;
    }

}
