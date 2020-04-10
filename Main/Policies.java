package Main;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Policies {

    private Document doc;
    private ArrayList<Policy> policies = new ArrayList<>();
    private ArrayList<Policy> policiesToRemove = new ArrayList<>();

    public Policies(Document doc) throws ParseException {
        this.doc = doc;
        this.parsePolicies(doc);
    }

    public ArrayList<Policy> getPolicies() {
        return policies;
    }

    public ArrayList<Policy> getPoliciesToRemove() {
        return policiesToRemove;
    }

    private void parsePolicies(Document doc) throws ParseException {
        policies.clear();

        NodeList nodes = doc.getElementsByTagName("policy");

        int id;
        String nameEntry;
        String[] nameElements;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date;

        for (int index = 0; index < nodes.getLength(); index++) {
            Node node = nodes.item(index);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                id = Integer.parseInt(element
                        .getElementsByTagName("id")
                        .item(0)
                        .getTextContent());

                nameEntry = element
                        .getElementsByTagName("name")
                        .item(0)
                        .getTextContent();

                nameElements = nameEntry.split(" ");
                if (nameElements[0].matches("\\d{4}-\\d{2}-\\d{2}")) {
                    date = dateFormat.parse(nameElements[0]);
                    Policy policy = new Policy(id, date);
                    policies.add(policy);
                }
                else {
                    element.getParentNode().removeChild(element);
                    index--;
                }
            }
        }
    }

    public Date getOldestPolicyDate() {
        Date oldestDate = new Date();
        for (Policy policy : policies) {
            if (oldestDate.after(policy.getDate())) {
                oldestDate = policy.getDate();
            }
        }
        return oldestDate;
    }

    public void removePolicies(String date) throws ParseException {
        policiesToRemove.clear();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date cutOffDate = dateFormat.parse(date);
        Policy policy;
        NodeList nodes = doc.getElementsByTagName("policy");
        int totalPolicies = nodes.getLength();
        int index = 0;
        while (index < totalPolicies) {
            Node node = nodes.item(index);
            policy = policies.get(index);
            if (cutOffDate.after(policy.getDate())) {
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    element.getParentNode().removeChild(element);
                    policiesToRemove.add(policy);
                    policies.remove(index);
                    totalPolicies--;
                    index = 0;
                }
            }
            else {
                index++;
            }
        }
    }
}
