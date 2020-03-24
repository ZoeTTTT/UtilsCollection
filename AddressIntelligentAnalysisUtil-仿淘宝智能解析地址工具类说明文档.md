### 仿淘宝智能解析地址工具类说明文档-AddressIntelligentAnalysisUtil
#### 1.输入参数及返回值说明</br>
输入参数：addressInfo-指即将被解析的地址</br>
返回值：```java Map<String,String> returnMap;```
```java
returnMap.put(Constant.MAP_KEY_PHONE_NUMBER, phoneValue); //手机号
returnMap.put(Constant.MAP_KEY_NAME, nameValue);  //姓名
returnMap.put(Constant.MAP_KEY_REGION_AREA, regionAreaValue);  //省市区合并的字符串，想要对应model稍作修改也可得到
returnMap.put(Constant.MAP_KEY_DETAIL_ADDRESS, detailAddressValue); //详细地址
``` 
#### 2.使用工具类需遇见准备的数据源说明
源码中</br>
```java
List<Province> proList =
            (List<Province>) SharePreferencesUtil.getInstance().getObject(Constant.SP_KEY_PROVINCE);
List<City> cityList = ((Map<String, List<City>>) SharePreferencesUtil.getInstance()
                .getObject(Constant.SP_KEY_CITY)).get(currentProvince.getUid());
List<County> areaList = ((Map<String, List<County>>) SharePreferencesUtil.getInstance()
                .getObject(Constant.SP_KEY_COUNTY)).get(currentCity.getUid());
```
其中涉及到的省市区的数据model为</br>
Province：
```java
String name;
String uid;
```
City:
```java
String name;
String provinceUid;
String uid;
```
County:
```java
String name;
String cityUid;
String uid;
```
省市区在缓存中存储的数据形式为(具体数据网上一般都有，这里数据是项目后端返回的)：</br>
```java
List<Province> proList:省及直辖市列表(即一级数据)
Map<String, List<City>>:市级数据(即二级数据)，其中key为provinceUid，value为此provinceUid对应的City列表
Map<String, List<County>>:区级数据(即三级数据)，其中key为cityUid，value为此cityUid对应的County列表
```
#### 3.解析逻辑说明及相关示例
```例如：
上海市徐汇区石龙路257号303,13278779999。肉一
13278779999。上海市徐汇区，石龙路257号303,肉一
```
step1:</br>考虑到每个人打字分隔习惯差异(有人喜欢逗号，有人喜欢句号，有人喜欢回车等等)，故第一步将地址内容中的所有符号统一替换为空格，即：</br>
```
上海市徐汇区石龙路257号303 13278779999 肉一
13278779999 上海市徐汇区 石龙路257号303 肉一
```
step2:</br>提取其中的电话号码，赋给phoneValue，并将其从原内容中移除并去掉字符串头尾的空格(考虑到电话号码可能写在最前或最后)，即：
```
上海市徐汇区石龙路257号303 肉一
上海市徐汇区 石龙路257号303 肉一
```
step3:</br>根据地址中省市区关键字提取省市区信息，即便地址中写的不够标准也可将省市区标准化，</br>
例如：上海徐汇 解析结果为：上海市市辖区徐汇区，并赋值给regionAreaValue<br>
因省市区可能存在书写不标准的情况，故此时无法移除原地址中的省市区内容。</br>

step4:</br>根据空格分割为多个字符串：
```
肉一 //长度小于4位视为姓名，赋值给nameValue，
上海市徐汇区石龙路257号303  //包含省市区的话移除省市区(这里因输入数据的不准确性可能会有切割不准确的情况)后赋值给detailAddressValue
石龙路257号303 //不包含的话直接赋值给detailAddressValue
```
总结：此工具类在详细地址中有很多空格或标点的情况下无法精确识别如：石龙路 257 号 303  
部分偏远地区少数名族地区识别精确度不高例如：内蒙古赤峰市翁牛特旗阿拉善盟等等，可在此基础上进行优化

