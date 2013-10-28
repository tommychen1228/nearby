package com.cdm.nearby.modal;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import com.cdm.nearby.R;
import com.cdm.nearby.common.L;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: cdm
 * Date: 2/13/13
 * Time: 2:45 PM
 */
public class CategoryManager extends BaseManger {

    public CategoryManager(Context context) {
        super(context);
    }

    public List<Category> getAll() {
        ArrayList<Category> categories = new ArrayList<Category>();

        LinkedList<String> tagStack = new LinkedList<String>();
        LinkedList<Category> categoryStack = new LinkedList<Category>();

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xmlResourceParser = factory.newPullParser();
            InputStream inputStream = context.getResources().openRawResource(R.raw.categories);
            xmlResourceParser.setInput(inputStream, "UTF-8");

            Category currentCategory = null;

            while (xmlResourceParser.getEventType() != XmlResourceParser.END_DOCUMENT) {
                //如果是开始标签
                if (xmlResourceParser.getEventType() == XmlResourceParser.START_TAG) {

                    //获取标签名称
                    String tag = xmlResourceParser.getName();
                    tagStack.addLast(tag);

                    if (tag.equals("category")) {
                        Category parent = categoryStack.peekLast();

                        Category category = new Category();

                        if (parent != null) {
                            parent.getChildren().add(category);
                        }

                        if (getTagPath(tagStack).equals("/categories/category")) {
                            categories.add(category);
                        }

                        currentCategory = category;

                        categoryStack.addLast(category);
                    }


                } else if (xmlResourceParser.getEventType() == xmlResourceParser.END_TAG) {
                    String tag = tagStack.pollLast();

                    if (tag.equals("category")) {
                        categoryStack.pollLast();
                    }

                } else if (xmlResourceParser.getEventType() == xmlResourceParser.TEXT) {
                    String tag = tagStack.peekLast();
                    String text = xmlResourceParser.getText();
                    if (tag.equals("name")) {
                        currentCategory.setName(text);
                    } else if (tag.equals("code")) {
                        currentCategory.setCode(text);
                    }
                }
                //下一个标签
                xmlResourceParser.next();
            }

        } catch (Exception e) {
            L.e(e.getMessage(), e);
        }

        return categories;
    }

    public Category getByCode(String code) throws Exception {

        InputSource inputSource = new InputSource(context.getResources().openRawResource(R.raw.categories));

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        String expression = "//category[code='" + code + "']";
        Node node = (Node) xpath.evaluate(expression, inputSource, XPathConstants.NODE);

        Category parent = new Category();
        parseNode(node, parent);

        return parent;
    }

    private void parseNode(Node node, Category parent){
        NodeList children = node.getChildNodes();
        for(int i = 0; i < children.getLength(); i++){
            Node child = children.item(i);

            String nodeName = child.getLocalName();
            String nodeValue = child.getTextContent();

            if(("name").equals(nodeName)){
                parent.setName(nodeValue);
            } else if (("code").equals(nodeName)){
                parent.setCode(nodeValue);
            } else if (("children").equals(nodeName)){


                NodeList grandchildren = child.getChildNodes();

                for(int j = 0; j < grandchildren.getLength(); j++){
                    Node grandChild = grandchildren.item(j);
                    String grandChildName = grandChild.getLocalName();
                    if("category".equals(grandChildName)){
                        Category category = new Category();
                        parent.getChildren().add(category);

                        parseNode(grandChild, category);
                    }

                }


            }
        }
    }

    private String getTagPath(List<String> tags) {
        StringBuffer path = new StringBuffer();
        for (String tag : tags) {
            path.append("/" + tag);
        }

        return path.toString();
    }
}
