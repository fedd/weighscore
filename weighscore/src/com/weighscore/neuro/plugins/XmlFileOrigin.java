package com.weighscore.neuro.plugins;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.*;
import com.weighscore.neuro.*;

public class XmlFileOrigin extends Origin {
    private final static String XPOINTERLINKSTART = "#xpointer(/neuralNetwork/neuron[";
    private static DocumentBuilderFactory dbf;




    // Origin methods
    public void initNeuralNetwork(NeuralNetworkLocal neuralNetwork, String fileName) {
        initNeuralNetworkByFile(neuralNetwork, getFile(fileName));
    }
    public void saveNeuralNetwork(NeuralNetworkLocal neuralNetwork) {
        File f = this.getFile(neuralNetwork.getName());
        try {
            FileOutputStream fw = new FileOutputStream(f);
            outputNeuralNetwork(neuralNetwork, fw);
            fw.close();
        } catch (IOException ex) {
            throw new NeuralException("Coudn't write neural network to file " + f, ex);
        }
    }
    public String[] getNeuralNetworkNames() {
        String path = getPath();
        File dir = new File(path);
        FilenameFilter ff = new FF(this);
        return dir.list(ff);
    }




    // XmlOrigin static methods needed to the library
    public static void initNeuralNetworkByInput(NeuralNetworkLocal neuralNetwork, InputStream in){
        Document dom;
        try{
            dom = inputDOM(in);
            initNeuralNetworkByDOM(neuralNetwork, dom);
        } catch (NeuralException e){
            throw new NeuralException("Coudn't parse neural network xml input stream" + ": " + e.getMessage());
        }
    }
    public static void outputNeuralNetwork(NeuralNetwork narea, OutputStream out){
        outputDOM(getNeuralNetworkDocument(narea), out);
    }
    public static void initTransByInput(Translator tran, InputStream in){
        Document dom;
        try{
            dom = inputDOM(in);
            initTransByElement(tran, (Element)dom.getElementsByTagName("translator").item(0));
        } catch (NeuralException e){
            throw new NeuralException("Coudn't parse translator input stream" + ": " + e.getMessage());
        }
    }
    public static void outputTranslator(Translator translator,  OutputStream out) {
        Document doc = getDocBld().newDocument();
        doc.appendChild(getTranslatorElement(translator, doc));
        outputDOM(doc, out);
    }


    // private helper methods
    private static void initNeuralNetworkByFile(NeuralNetworkLocal neuralNetwork, File in){
        Document dom;
        try{
            dom = inputDOM(in);
            initNeuralNetworkByDOM(neuralNetwork, dom);
        } catch (NeuralException e){
            throw new NeuralException("Coudn't parse neural network xml file " + in.getName() + ": " + e.getMessage(), e);
        }
    }

    // initializing objects by DOM and saving objects to DOM
    private static synchronized void initNeuralNetworkByDOM(NeuralNetworkLocal narea, Document doc){

        // устанавливаем скорость обучения
        Element root = doc.getDocumentElement();
        if(!root.getTagName().equalsIgnoreCase("neuralNetwork")){
            throw new InvalidNeuralDefinitionException ("Xml document misses neuralNetwork tag; may be it is not a neural network definition document");
        }

        // теперь трансялтор находится прям в нейросети
        //narea.setTranslatorByName(root.getAttribute("translator"));
        // ********************************************************
        NodeList tranell = root.getElementsByTagName("translator");
        if (tranell.getLength() > 0) {
            Element tranel = (Element) tranell.item(0);
            Translator tran = new Translator();
            initTransByElement(tran, tranel);
            narea.setTranslator(tran);
        }

        // находим teacher
        Teacher teacher = (Teacher)getParameterHolder(root, "teacher", null);
        narea.setTeacher(teacher);

        // добваляем все нейроны по очереди
        NodeList nrns = root.getElementsByTagName("neuron");
        for (int i = 0; i < nrns.getLength(); i++) {
            Element eln = (Element) nrns.item(i);
            Neuron nn = narea.addNeuron();

            // устанавливаем порог
            String thr = eln.getAttribute("threshold");
            if (thr.length() == 0) {
                narea.initWeightOrThreshold(nn); //nn.initThreshold();
            } else {
                narea.setWeightOrThreshold(nn, Double.parseDouble(thr)); //nn.setThreshold(Double.parseDouble(thr));
            }

            // устанавливаем флаг вход-выход
            if (eln.getAttribute("input").equals("true")) {
                narea.markAsInput(nn);
            }
            if (eln.getAttribute("output").equals("true")) {
                narea.markAsOutput(nn);
            }

            // устанавливаем активационную функцию
            Activation afun = (Activation)getParameterHolder(eln, "activation", null);
            narea.setActivation(nn, afun);
            //nn.setActivation(afun);

            // вспоминаем статистику
            Statistic stat = (Statistic)getParameterHolder(eln, "statistic", nn);
            //narea.setStatistic(nn, stat);
            //nn.setStatistic(stat);
        }

        // проходим еще раз и устанавливаем связи
        for (int i = 0; i < nrns.getLength(); i++) {
            Element eln = (Element) nrns.item(i);
            Element axel = (Element) eln.getElementsByTagName("axon").item(0);
            if (axel!=null){
                NodeList synels = axel.getElementsByTagName("synapse");
                for (int j = 0; j < synels.getLength(); j++) {
                    Element synel = (Element) synels.item(j);
                    String xPointer = synel.getAttribute("xlink:href");
                    if (!xPointer.startsWith(XPOINTERLINKSTART)) {
                        throw new NeuralException("Malformed synapse neuron xlink; must start with \"" + XPOINTERLINKSTART + "\"");
                    } else {
                        String ind = xPointer.substring(XPOINTERLINKSTART.length(),
                                                        xPointer.indexOf(']',
                                XPOINTERLINKSTART.length()));
                        int index = Integer.parseInt(ind)-1;
                        narea.addSynapse(narea.getNeurons()[i], narea.getNeurons()[index]);
                        //narea.getNeurons()[i].addOutSynapseTo(narea.getNeurons()[index]);

                        Synapse syn = narea.getNeurons()[i].getOutSynapses()[j];
                        // устанавливаем вес
                        String w = synel.getAttribute("weight");
                        if (w.length() == 0) {
                            narea.initWeightOrThreshold(syn);
                            //syn.initWeight();
                        } else {
                            narea.setWeightOrThreshold(syn, Double.parseDouble(w));
                            //syn.setWeight(Double.parseDouble(w));
                        }
                        // вспоминаем статистику
                        Statistic stat = (Statistic)getParameterHolder(synel, "statistic", syn);
                        //narea.setStatistic(syn, stat);
                        //syn.setStatistic(stat);
                    }
                }
            }
        }
    }
    private static synchronized void initTransByElement(Translator tran, Element root){
        //Element root = doc.getDocumentElement();
        //if(!root.getTagName().equalsIgnoreCase("translator")){
        //    throw new InvalidNeuralDefinitionException("Xml document misses translator tag; may be it is not a translator definition document");
        //}

        // проходим по описаниям полей
        NodeList fields = root.getElementsByTagName("field");
        int l = fields.getLength();
        if(l==0){
            throw new InvalidNeuralDefinitionException("Translator definition document defines no fields");
        }
        tran.field = new String[l];
        tran.ispass = new boolean[l];
        tran.dividor = new double[l];
        tran.inputval = new String[l][];
        tran.activator = new int[l][];
        tran.outputrange_max = new double[l][];
        tran.outputrange_min = new double[l][];
        tran.outputrange_val = new double[l][];
        tran.backActivator = new int[2][];

        Hashtable inpbackact = new Hashtable(), outbackact = new Hashtable();

        // теперь проходим.
        for (int i = 0; i<l; i++){
            Element fel = (Element)fields.item(i);
            tran.field[i] = fel.getAttribute("name");
            int ans = 0;
            if(fel.getAttribute("type").equals("answer")){
                ans=1;
            }else{
                tran.askfields++;
            }
            NodeList passes = fel.getElementsByTagName("pass");
            if(passes.getLength()>0){
                Element pass = (Element) passes.item(0);
                NodeList  acts = pass.getElementsByTagName("activator");
                Element act = (Element) acts.item(0);

                tran.ispass[i]=true;

                int neuronnum = Integer.parseInt(act.getAttribute("index")) - 1;
                tran.activator[i] = new int[1];
                tran.activator[i][0] = neuronnum;
                //backActivator[ans][neuronnum] = i;
                if (ans==0)
                    inpbackact.put(new Integer(neuronnum), new Integer(i));
                else
                    outbackact.put(new Integer(neuronnum), new Integer(i));
                String div = pass.getAttribute("dividor");
                if(div.length()==0)
                    tran.dividor[i]=1;
                else
                    tran.dividor[i]=Double.parseDouble(div);

            }else{
                NodeList translates = fel.getElementsByTagName("translate");
                if(translates.getLength()>0){
                    Element translate = (Element) translates.item(0);
                    NodeList items = translate.getElementsByTagName("item");

                    int len = items.getLength();
                    tran.activator[i] = new int[len];
                    tran.inputval[i]=new String[len];

                    tran.outputrange_max[i] = new double[len];
                    tran.outputrange_min[i] = new double[len];
                    tran.outputrange_val[i] = new double[len];

                    for(int it=0; it<len; it++){
                        Element item = (Element) items.item(it);
                        tran.inputval[i][it] = item.getAttribute("value");

                        Element act = (Element)item.getElementsByTagName("activator").item(0);
                        int neuronnum = Integer.parseInt(act.getAttribute("index")) - 1;
                        tran.activator[i][it] = neuronnum;
                        //backActivator[ans][neuronnum] = i;
                        if (ans==0)
                            inpbackact.put(new Integer(neuronnum), new Integer(i));
                        else
                            outbackact.put(new Integer(neuronnum), new Integer(i));

                        Element range = (Element)act.getElementsByTagName("range").item(0);
                        tran.outputrange_val[i][it] = Double.parseDouble(range.getAttribute("value"));
                        String m = range.getAttribute("min");
                        if(m.length()==0)
                            tran.outputrange_min[i][it] = Double.MAX_VALUE*-1;
                        else
                            tran.outputrange_min[i][it] = Double.parseDouble(m);
                        m = range.getAttribute("max");
                        if(m.length()==0)
                            tran.outputrange_max[i][it] = Double.MAX_VALUE;
                        else
                            tran.outputrange_max[i][it] = Double.parseDouble(m);
                    }
                }
            }
        }
        // back activator initialization
        // определение количества и номеров входящих нейронов
        Enumeration en = inpbackact.keys();
        while(en.hasMoreElements()){
            int tmp = ((Integer)en.nextElement()).intValue();
            if (tran.asknrns<tmp)
                tran.asknrns=tmp;
        }
        tran.asknrns++;
        tran.backActivator[0] = new int[tran.asknrns];
        en = inpbackact.elements();
        en = inpbackact.keys();
        while(en.hasMoreElements()){
            Integer Neuronnum = (Integer)en.nextElement();
            int neuronnum = Neuronnum.intValue();
            int fieldnum = ((Integer)inpbackact.get(Neuronnum)).intValue();
            tran.backActivator[0][neuronnum]=fieldnum;
        }
        // определение количества и номеров исходящих нейронов
        en = outbackact.keys();
        while(en.hasMoreElements()){
            int tmp = ((Integer)en.nextElement()).intValue();
            if (tran.ansnrns<tmp)
                tran.ansnrns=tmp;
        }
        tran.ansnrns++;
        tran.backActivator[1] = new int[tran.ansnrns];
        en = outbackact.elements();
        en = outbackact.keys();
        while(en.hasMoreElements()){
            Integer Neuronnum = (Integer)en.nextElement();
            int neuronnum = Neuronnum.intValue();
            int fieldnum = ((Integer)outbackact.get(Neuronnum)).intValue();
            tran.backActivator[1][neuronnum]=fieldnum;
        }

    }
    private static synchronized Element getTranslatorElement(Translator tr, Document doc) {
        //throw new UnsupportedOperationException("The method \"getTranslatorDocument\" is not yet implemented");

        // создаем новый документ
        //Document doc = getDocBld().newDocument();
        Element trel = doc.createElement("translator");
        //doc.appendChild(trel);

        for(int i=0; i<tr.field.length; i++){
            Element fld = doc.createElement("field");
            trel.appendChild(fld);

            fld.setAttribute("name", tr.field[i]);
            fld.setAttribute("type", (i<tr.askfields?"ask":"answer"));

            if(tr.ispass[i]){
                Element pas = doc.createElement("pass");
                fld.appendChild(pas);

                if(tr.dividor[i]!=1){
                    pas.setAttribute("dividor", String.valueOf(tr.dividor[i]));
                }
                Element act = doc.createElement("activator");
                pas.appendChild(act);
                act.setAttribute("index", String.valueOf(tr.activator[i][0]+1));
            }else{
                Element tra = doc.createElement("translate");
                fld.appendChild(tra);

                int[] items = tr.activator[i];
                for(int j = 0; j<items.length;j++){
                    Element ite = doc.createElement("item");
                    tra.appendChild(ite);
                    ite.setAttribute("value", tr.inputval[i][j]);

                    Element act = doc.createElement("activator");
                    ite.appendChild(act);
                    act.setAttribute("index", String.valueOf(tr.activator[i][j]+1));


                    Element ran = doc.createElement("range");
                    act.appendChild(ran);
                    ran.setAttribute("value", String.valueOf(tr.outputrange_val[i][j]));

                    double mm = tr.outputrange_min[i][j];
                    if(mm != Double.MAX_VALUE*-1){
                        ran.setAttribute("min", String.valueOf(mm));
                    }
                    mm = tr.outputrange_max[i][j];
                    if(mm != Double.MAX_VALUE){
                        ran.setAttribute("max", String.valueOf(mm));
                    }
                }
            }
        }
        return trel;
    }
    private static synchronized Document getNeuralNetworkDocument(NeuralNetwork narea){

        //NeuralNetworkAbstract narea = getNeuralNetwork(name);

        // создаем новый документ
        Document doc = getDocBld().newDocument();
        Element neuralNetwork = doc.createElement("neuralNetwork");
        //Element neuralNetwork = getParameterHolderElement(doc, (ParameterHolder)narea, "neuralNetwork");
        doc.appendChild(neuralNetwork);


        if (narea.getTranslator()!=null){
            neuralNetwork.appendChild(getTranslatorElement(narea.getTranslator(), doc));
        }

        //String t = narea.getTranslatorName();
        //if(t!=null)
        //    neuralNetwork.setAttribute("translator", t);

        neuralNetwork.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
        //neuralNetwork.setAttributeNS("http://www.w3.org/1999/xlink", "xmlns:xlink", "");

        // teacher
        Element teacher = getParameterHolderElement(doc, narea.getTeacher(),
                                                    "teacher");
        neuralNetwork.appendChild(teacher);

        // выводим все нейроны в xml
        Neuron[] nrns = narea.getNeurons();
        for (int i = 0; i < nrns.length; i++) {
            Neuron nrn = nrns[i];
            Element neuron = doc.createElement("neuron");
            neuralNetwork.appendChild(neuron);

            // порог
            neuron.setAttribute("threshold", String.valueOf(nrns[i].getWeight()));
            //neuron.setAttribute("threshold", String.valueOf(nrns[i].getThreshold()));

            // вход-выход
            if (nrns[i].isInput()) {
                neuron.setAttribute("input", "true");
            }
            if (nrns[i].isOutput()) {
                neuron.setAttribute("output", "true");
            }

            // функция активации
            Element actFun = getParameterHolderElement(doc, nrn.getActivation(),
                    "activation");
            neuron.appendChild(actFun);

            // аксон - добавление синапсов
            Synapse[] syns = nrn.getOutSynapses();
            if (syns.length > 0) {
                Element axon = doc.createElement("axon");
                neuron.appendChild(axon);
                // проход по синапсам
                for (int ii = 0; ii < syns.length; ii++) {
                    Synapse syn = syns[ii];
                    Element synapse = doc.createElement("synapse");
                    axon.appendChild(synapse);

                    // вес
                    synapse.setAttribute("weight", String.valueOf(syn.getWeight()));

                    // установление связи с нейроном c помощью xPointer
                    synapse.setAttributeNS("http://www.w3.org/1999/xlink",
                                           "xlink:type", "locator");
                    int ind = (syn.getOutNeuron().getIndex() + 1);
                    String xpoint = XPOINTERLINKSTART + ind + "])";
                    synapse.setAttributeNS("http://www.w3.org/1999/xlink",
                                           "xlink:href", xpoint);

                    // статистика
                    Element stats = getParameterHolderElement(doc,
                            syn.getMultiStats(), "statistic");
                    synapse.appendChild(stats);
                }
            }

            // запись статистики
            Element stats = getParameterHolderElement(doc, nrn.getMultiStats(),
                    "statistic");
            neuron.appendChild(stats);
        }

        doc.normalize();

        return doc;
    }

    // DOM and file utility methods
    private static DocumentBuilder getDocBld(){
        if(dbf==null){
            dbf = DocumentBuilderFactory.newInstance();
        }
        DocumentBuilder db;
        try{
            db = dbf.newDocumentBuilder();
        }
        catch(ParserConfigurationException pce){
            throw new NeuralException("Xml parser configuration problem", pce);
        }
        return db;
    }
    private static Document inputDOM(File in){
        DocumentBuilder db = getDocBld();
        Document doc;
        try {
            doc = db.parse(in);
        } catch (Exception ex) {
            throw new NeuralException("Couldn't load xml file: "+ ex.toString(), ex);
        }
        return doc;
    }
    private static Document inputDOM(InputStream in){
        DocumentBuilder db = getDocBld();
        Document doc;
        try {
            doc = db.parse(in);
        } catch (Exception ex) {
            throw new NeuralException("Couldn't load xml stream: "+ ex.toString(), ex);
        }
        return doc;
    }
    private static void outputDOM(Document doc, OutputStream out){
        DOMSource src = new DOMSource(doc);
        StreamResult dst = new StreamResult(out);

        TransformerFactory tf = TransformerFactory.newInstance();
        //tf.setAttribute("indent-number", new Integer(2));
        Transformer tr;
        try {
            tr = tf.newTransformer();

        } catch (TransformerConfigurationException ex) {
            throw new NeuralException("Xml transformer configuration problem", ex);
        }
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        //tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "NeuralNetwork.dtd");
        tr.setOutputProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");
        try {
            tr.transform(src, dst);
        } catch (TransformerException ex1) {
            throw new NeuralException("Xml transform problem", ex1);
        }
    }

    private static FileInputStream getFileInputStream(String path, String fileName){
        File NeuralNetworkXmlFile = new File(path, fileName);
        try {
            FileInputStream fis = new FileInputStream(NeuralNetworkXmlFile);
            return fis;
        } catch (FileNotFoundException ex) {
            throw new NeuralException("Coudn't read from file " + fileName, ex);
        }
    }
    private File getFile(String fileName){
        File NeuralNetworkXmlFile = new File(getPath(), fileName);
        return NeuralNetworkXmlFile;
    }
    // get path property
    private String getPath(){
        String path;
        try{
            path = ((String)super.getProperties().get("neuralNetwork.origin.xml.path")).trim();
            if(path==null)
                path="./";
            else if(!path.endsWith("/"))
                path=path+"/";
        }
        catch(NullPointerException e){
            path="./";
        }
        return path;
    }

    // parameterholder parsing and saving methods
    private static Element getParameterHolderElement(Document doc, ParameterHolder ph, String tagname){
        Element el = doc.createElement(tagname);
        if(ph instanceof MultiStats){
            MultiStats ms = (MultiStats)ph;
            StringBuffer sb = new StringBuffer();
            String[] sts = ms.getStatisticNames();
            for(int i=0; i<sts.length;i++){
                if(i!=0)
                    sb.append(' ');
                sb.append(sts[i]);
            }
            String clnm = sb.toString();

            el.setAttribute("classes", clnm);
        }
        else
            el.setAttribute("class", Util.getPluginNameForClass(ph.getClass()));

        String[] pnames = ph.getParameterNames();
        for(int i=0; i<pnames.length; i++){
            Element param = doc.createElement("parameter");
            el.appendChild(param);
            param.setAttribute("name", pnames[i]);
            param.appendChild(doc.createTextNode(ph.getParameterAsString(pnames[i])));
        }
        return el;
    }
    private static ParameterHolder getParameterHolder(Element element, String tagname, WeightHolder whr){
        NodeList afell = element.getChildNodes();
        for(int i=0;i<afell.getLength(); i++){
            Node node = afell.item(i);
            Element afel;
            if(node instanceof Element){
                afel = (Element)node;
            }
            else{
                continue;
            }
            if (afel.getTagName().equals(tagname)) {
                String afname = afel.getAttribute("class");
                ParameterHolder af = null;
                if(afname.length()!=0){
                    try {
                        af = (ParameterHolder) (Util.getPluginClassForName(
                                afname)).newInstance();
                    } catch (Exception ex) {
                        throw new NeuralException(
                                "Couldn't initialize " + afname, ex);
                    }
                }else{
                    afname = afel.getAttribute("classes");
                    if(afname.length()!=0){
                        String[]stats = afname.split("\\s");
                        af = whr.getMultiStats();
                        for(int j=0; j< stats.length;j++){
                            whr.getMultiStats().addStatistic(stats[j]);
                        }
                    }
                }
                // инициализируем объект
                if (af != null) {
                    NodeList parm = afel.getElementsByTagName("parameter");
                    for (int p = 0; p < parm.getLength(); p++) {
                        Element parmel = (Element) parm.item(p);
                        String parname = parmel.getAttribute("name");
                        Node nde = parmel.getFirstChild();
                        if(nde!=null){
                            String parval = parmel.getFirstChild().getNodeValue();
                            af.setParameter(parname, parval);
                        }
                    }
                }
                return af;
            }
        }
        return null;
    }

    public synchronized boolean doesExist(String fileName) {
        return getFile(fileName).exists();
    }
    public class FF implements FilenameFilter{
        String ext;
        FF(XmlFileOrigin xfo){
            Properties p = xfo.getProperties();
            String s = p.getProperty("neuralNetwork.origin.xml.extension");
            this.ext = s.trim();
        }
        public boolean accept(File dir, String name) {
            return name.endsWith(ext);
        }

    }
}
