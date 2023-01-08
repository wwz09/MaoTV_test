#!/bin/bash
#添加PY支持
wget --no-check-certificate -qO- "https://raw.githubusercontent.com/UndCover/PyramidStore/main/aar/pyramid-1011.aar" -O app/libs/pyramid.aar
sed -i "/thunder.jar/a\    implementation files('libs@pyramid.aar')" app/build.gradle
sed -i 's#@#\\#g' app/build.gradle
sed -i 's#pyramid#\\pyramid#g' app/build.gradle
echo "" >>app/proguard-rules.pro
echo "" >>app/proguard-rules.pro
echo "#添加PY支持" >>app/proguard-rules.pro
echo "-keep public class com.undcover.freedom.pyramid.** { *; }" >>app/proguard-rules.pro
echo "-dontwarn com.undcover.freedom.pyramid.**" >>app/proguard-rules.pro
echo "-keep public class com.chaquo.python.** { *; }" >>app/proguard-rules.pro
echo "-dontwarn com.chaquo.python.**" >>app/proguard-rules.pro
sed -i '/import com.orhanobut.hawk.Hawk;/a\import com.undcover.freedom.pyramid.PythonLoader;' app/src/main/java/com/github/tvbox/osc/base/App.java
sed -i '/import com.orhanobut.hawk.Hawk;/a\import com.github.catvod.crawler.SpiderNull;' app/src/main/java/com/github/tvbox/osc/base/App.java
sed -i '/PlayerHelper.init/a\        PythonLoader.getInstance().setApplication(this);' app/src/main/java/com/github/tvbox/osc/base/App.java
sed -i '/import android.util.Base64;/a\import com.github.catvod.crawler.SpiderNull;' app/src/main/java/com/github/tvbox/osc/api/ApiConfig.java
sed -i '/import android.util.Base64;/a\import com.undcover.freedom.pyramid.PythonLoader;' app/src/main/java/com/github/tvbox/osc/api/ApiConfig.java
sed -i '/private void parseJson(String apiUrl, String jsonStr)/a\        PythonLoader.getInstance().setConfig(jsonStr);' app/src/main/java/com/github/tvbox/osc/api/ApiConfig.java
sed -i '/public Spider getCSP(SourceBean sourceBean)/a\        if (sourceBean.getApi().startsWith(\"py_\")) {\n        try {\n            return PythonLoader.getInstance().getSpider(sourceBean.getKey(), sourceBean.getExt());\n        } catch (Exception e) {\n            e.printStackTrace();\n            return new SpiderNull();\n        }\n    }' app/src/main/java/com/github/tvbox/osc/api/ApiConfig.java
sed -i '/public Object\[\] proxyLoca/a\    try {\n        if(param.containsKey(\"api\")){\n            String doStr = param.get(\"do\").toString();\n            if(doStr.equals(\"ck\"))\n                return PythonLoader.getInstance().proxyLocal(\"\",\"\",param);\n            SourceBean sourceBean = ApiConfig.get().getSource(doStr);\n            return PythonLoader.getInstance().proxyLocal(sourceBean.getKey(),sourceBean.getExt(),param);\n        }else{\n            String doStr = param.get(\"do\").toString();\n            if(doStr.equals(\"live\")) return PythonLoader.getInstance().proxyLocal(\"\",\"\",param);\n        }\n    } catch (Exception e) {\n        e.printStackTrace();\n    }\n' app/src/main/java/com/github/tvbox/osc/api/ApiConfig.java


cp -rf $GITHUB_WORKSPACE/DIY/png/图标7.png app/src/main/res/drawable/app_icon.png
cp -rf $GITHUB_WORKSPACE/DIY/png/等待图标.png app/src/main/res/drawable/icon_loading.png
cp -rf $GITHUB_WORKSPACE/DIY/tbox/taka旧主界面.xml app/src/main/res/layout/fragment_user.xml
cp -rf $GITHUB_WORKSPACE/DIY/tbox/activity_home_top界面.xml app/src/main/res/layout/activity_home.xml
cp -rf $GITHUB_WORKSPACE/DIY/qbox/epg_data.json app/src/main/assets/epg_data.json
sed -i 's/MM月dd日/yyyy年 MM月 dd日/g' app/src/main/res/values-zh/strings.xml
sed -i 's%TVBox%大仙TV%g' app/src/main/res/values-zh/strings.xml
sed -i 's%com.github.tvbox.osc%com.github.tvbox.osc.${{ matrix.osName }}%g' app/build.gradle
sed -i 's%HOME_REC, 2%HOME_REC, 1%g' app/src/main/java/com/github/tvbox/osc/base/App.java  
sed -i 's%HomeActivity.getRes().getString(R.string.app_source));% "http://gg.gg/12uv0a");%g' app/src/main/java/com/github/tvbox/osc/api/ApiConfig.java     

#首页多排
cp -rf $GITHUB_WORKSPACE/DIY/tbox/dialog_select.xml app/src/main/res/layout/dialog_select.xml
cp -rf $GITHUB_WORKSPACE/DIY/tbox/HomeActivity.java app/src/main/java/com/github/tvbox/osc/ui/activity/HomeActivity.java
#设置
cp -rf $GITHUB_WORKSPACE/DIY/tbox/fragment_model.xml app/src/main/res/layout/fragment_model.xml
#设置源多排
cp -rf $GITHUB_WORKSPACE/DIY/tbox/ModelSettingFragment.java app/src/main/java/com/github/tvbox/osc/ui/fragment/ModelSettingFragment.java

sed -i 's/6666/还原旧界面，增加首页多排/g'  app/src/main/res/layout/dialog_version.xml

#首页排版边框
sed -i 's/vs_30/vs_15/g' app/src/main/res/layout/dialog_select.xml

#FongMi的jar支持
echo "" >>app/proguard-rules.pro
echo "-keep class com.google.gson.**{*;}" >>app/proguard-rules.pro

echo 'DIY end'
