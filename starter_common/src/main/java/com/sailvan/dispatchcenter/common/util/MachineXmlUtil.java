package com.sailvan.dispatchcenter.common.util;

import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.Machine;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MachineXmlUtil {

    public static void main(String[] args) {
//        Long start = System.currentTimeMillis();
//        createXml(null);
//        System.out.println("运行时间："+ (System.currentTimeMillis() - start));
        List<String> list = new ArrayList<>();

        for (int i = 1; i <=100; i++) {
            list.add(i+"");
        }
        int size = list.size();
        for (int i = 0; i <=(size)/50; i++) {
            int start = i*50;
            int end = 50 * (i + 1);
            if(end >= size){
                end = size;
            }
            List<String> subList = list.subList(start, end);
            System.out.println(subList.toString());
        }

    }

    private static Map<String,List<String>> getMachineListMerge(List<Machine> machineAll){
        Map<String,List<String>> map = new HashMap<>();
        for (Machine machine : machineAll) {
            if(StringUtils.isEmpty(machine.getIp()) || machine.getStatus() == Constant.STATUS_INVALID){
                continue;
            }
            String key = machine.getUsername()+"___"+machine.getPassword();
            List<String> list = new ArrayList<>();
            if (!map.containsKey(key)) {
                list.add(JSONObject.toJSONString(machine));
            }else {
                list = map.get(key);
                list.add(JSONObject.toJSONString(machine));
            }
            map.put(key, list);
        }
        return map;
    }

    /**
     * 生成xml方法
     */
    public static File createXml(List<Machine> machineAll){
        Map<String,List<String>> map = getMachineListMerge(machineAll);
        if(map == null || map.isEmpty()){
            return null;
        }
        try {
            // 创建解析器工厂
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = factory.newDocumentBuilder();
            Document document = db.newDocument();
            document.setXmlStandalone(true);
            Element multiDesk = document.createElement("MultiDesk");
            // 向bookstore根节点中添加子节点book
            Element servers = document.createElement("Servers");

            Element properties = document.createElement("Properties");
            Element name = document.createElement("Name");
            properties.appendChild(name);

            Element description = document.createElement("Description");
            properties.appendChild(description);

            Element inheritGeneral = document.createElement("InheritGeneral");
            inheritGeneral.setTextContent("1");
            properties.appendChild(inheritGeneral);

            Element groupCollapsed = document.createElement("GroupCollapsed");
            groupCollapsed.setTextContent("0");
            properties.appendChild(groupCollapsed);
            servers.appendChild(properties);
            for (Map.Entry<String, List<String>> stringListEntry : map.entrySet()) {
                String key = stringListEntry.getKey();
                List<String> value = stringListEntry.getValue();
                Element serversGroup = getServersGroup(document, key);
                servers.appendChild(serversGroup);
                int size = value.size();
                if(size>50){
                    for (int i = 0; i <=(size)/50; i++) {
                        int start = i*50;
                        int end = 50 * (i + 1);
                        if(end >= size){
                            end = size;
                        }
                        List<String> subList = value.subList(start, end);
                        if (subList == null){
                            continue;
                        }
                        serversGroup.appendChild(getGroupGroup(document, subList, i));
//                        servers.appendChild(getServersGroup(document,key,subList, i));
                    }
                }else {
                    serversGroup.appendChild(getGroupGroup(document, value, 0));
//                    servers.appendChild(getServersGroup(document,key,value,0));
                }
            }


            multiDesk.appendChild(servers);
            // 将bookstore节点（已包含book）添加到dom树中
            document.appendChild(multiDesk);

            // 创建TransformerFactory对象
            TransformerFactory tff = TransformerFactory.newInstance();
            // 创建 Transformer对象
            Transformer tf = tff.newTransformer();

            // 输出内容是否使用换行
            tf.setOutputProperty(OutputKeys.VERSION, "1.0");
            tf.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            tf.setOutputProperty(OutputKeys.STANDALONE, "yes");
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","4");
            // 创建xml文件并写入内容
            tf.transform(new DOMSource(document), new StreamResult(new File("MultiDesk.xml")));
            System.out.println("MultiDesk.xml成功");
            return new File("MultiDesk.xml");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("MultiDesk.xml失败");
        }
        return null;
    }


    private static Element getServersGroup(Document document, String key){
        String username = key.split("___")[0];
        String password = key.split("___")[1];
        Element group = document.createElement("Group");


        Element group_Properties = document.createElement("Properties");


        Element group_Properties_name = document.createElement("Name");
        group_Properties_name.setTextContent(password);
        group_Properties.appendChild(group_Properties_name);

        Element group_Properties_description = document.createElement("Description");
        group_Properties.appendChild(group_Properties_description);

        Element group_Properties_inheritGeneral = document.createElement("InheritGeneral");
        group_Properties_inheritGeneral.setTextContent("0");
        group_Properties.appendChild(group_Properties_inheritGeneral);

        Element userName = document.createElement("UserName");
        userName.setTextContent(username);
        group_Properties.appendChild(userName);

        Element domain = document.createElement("Domain");
        group_Properties.appendChild(domain);

        Element Password = document.createElement("Password");
        Password.setTextContent(password);
        group_Properties.appendChild(Password);

        Element RDPPort = document.createElement("RDPPort");
        RDPPort.setTextContent("3389");
        group_Properties.appendChild(RDPPort);

        Element GroupCollapsed = document.createElement("GroupCollapsed");
        group_Properties.appendChild(GroupCollapsed);


        group.appendChild(group_Properties);
//        group.appendChild(getGroupGroup(document, list, index));

        return group;

    }


    private static Element getGroupGroup(Document document,List<String> list, int index){
        Element group = document.createElement("Group");


        Element group_Properties = document.createElement("Properties");

        Element group_Properties_name = document.createElement("Name");
        group_Properties_name.setTextContent(""+index);
        group_Properties.appendChild(group_Properties_name);

        Element group_Properties_description = document.createElement("Description");
        group_Properties.appendChild(group_Properties_description);

        Element group_Properties_inheritGeneral = document.createElement("InheritGeneral");
        group_Properties_inheritGeneral.setTextContent("1");
        group_Properties.appendChild(group_Properties_inheritGeneral);

        Element groupCollapsed = document.createElement("GroupCollapsed");
        groupCollapsed.setTextContent("1");
        group_Properties.appendChild(groupCollapsed);

        group.appendChild(group_Properties);
        for (String s : list) {
            Machine machine = JSONObject.parseObject(s, Machine.class);

            group.appendChild(getServer(document,machine));
        }

        return group;

    }


    private static Element getServer(Document document,Machine machine){
        Element server = document.createElement("Server");

        Element server_name = document.createElement("Name");
        server_name.setTextContent(machine.getIp());
        server.appendChild(server_name);

        Element server_description = document.createElement("Description");
        server.appendChild(server_description);

        Element server_Server = document.createElement("Server");
        server_Server.setTextContent(machine.getIp());
        server.appendChild(server_Server);

        Element inheritGeneral = document.createElement("InheritGeneral");
        inheritGeneral.setTextContent("1");
        server.appendChild(inheritGeneral);

        Element connectToServerConsole = document.createElement("ConnectToServerConsole");
        connectToServerConsole.setTextContent("0");
        server.appendChild(connectToServerConsole);

        Element desktopHeight = document.createElement("DesktopHeight");
        desktopHeight.setTextContent("0");
        server.appendChild(desktopHeight);

        Element desktopWidth = document.createElement("DesktopWidth");
        desktopWidth.setTextContent("0");
        server.appendChild(desktopWidth);

        Element colorDepth = document.createElement("ColorDepth");
        colorDepth.setTextContent("16");
        server.appendChild(colorDepth);

        Element fullScreen = document.createElement("FullScreen");
        fullScreen.setTextContent("0");
        server.appendChild(fullScreen);

        Element redirectPrinters = document.createElement("RedirectPrinters");
        redirectPrinters.setTextContent("0");
        server.appendChild(redirectPrinters);

        Element redirectClipboard = document.createElement("RedirectClipboard");
        redirectClipboard.setTextContent("1");
        server.appendChild(redirectClipboard);

        Element redirectPorts = document.createElement("RedirectPorts");
        redirectPorts.setTextContent("0");
        server.appendChild(redirectPorts);

        Element redirectSmartCards = document.createElement("RedirectSmartCards");
        redirectSmartCards.setTextContent("0");
        server.appendChild(redirectSmartCards);

        Element redirectDrives = document.createElement("RedirectDrives");
        redirectDrives.setTextContent("0");
        server.appendChild(redirectDrives);

        Element driveCollection = document.createElement("DriveCollection");
        server.appendChild(driveCollection);

        Element audioRedirectionMode = document.createElement("AudioRedirectionMode");
        audioRedirectionMode.setTextContent("0");
        server.appendChild(audioRedirectionMode);

        Element keyboardHookMode = document.createElement("KeyboardHookMode");
        keyboardHookMode.setTextContent("1");
        server.appendChild(keyboardHookMode);

        Element performanceFlags = document.createElement("PerformanceFlags");
        performanceFlags.setTextContent("384");
        server.appendChild(performanceFlags);

        Element bitmapPersistence = document.createElement("BitmapPersistence");
        bitmapPersistence.setTextContent("1");
        server.appendChild(bitmapPersistence);

        return server;

    }
}
