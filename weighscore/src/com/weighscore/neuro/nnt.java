package com.weighscore.neuro;


import java.io.*;
import java.util.*;
import java.sql.*;
//import org.apache.commons.cli.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: Vsetech</p>
 *
 * @author not attributable
 * @version 1.0
 */
public class nnt {
    private final static String CONFIG = "nnt.config";
    private final static String DESCRIPTION = "The Weighscore Neural Network training, probing and requesting command line\ntool capable to connect to the JDBC databases.";
    private final static String NAME = "Weighscore Neural Network Command Line Tool";

    private final static String SELECTSQL = "select * from {table}";
    private final static String UPDATESQL = "update {table} set {value} where {condition}";

    private final static String TABLE = "{table}";
    private final static String VALUE = "{value}";
    private final static String COND = "{condition}";

    private boolean test, ask, /*teach, */verbose;
    NeuralNetwork nn;
    Properties ppp;
    String saveas=null;

    String[] queAns, answ;


    public static void main(String[] args) {

        try{

            System.out.print(NAME);
            System.out.print('.');
            System.out.print(' ');
            System.out.println(NeuralNetworkLocal.COPYRIGHT);
            System.out.println(NeuralNetworkLocal.WELCOME);

            nnt me = new nnt();

            Vector leftArgs = new Vector();
            me.ppp = me.getConfig(args, leftArgs);
            me.queAns = new String[leftArgs.size()];
            //me.queAns = (String[]) leftArgs.toArray(me.queAns);
            leftArgs.copyInto(me.queAns);

            /*Enumeration enu = me.ppp.propertyNames();
                     while(enu.hasMoreElements()){
                String n = (String) enu.nextElement();
                System.out.print(n);
                System.out.print('\t');
                System.out.println(me.ppp.getProperty(n));

                     }
                     System.out.println("----------");
                     for(int i=0;i<me.queAns.length;i++){
                System.out.println(me.queAns[i]);
                     }*/



            me.verbose = (me.ppp.getProperty("verbose") != null);

            me.test = (me.ppp.getProperty("test") != null);

            me.ask = (me.ppp.getProperty("ask") != null);

            //me.teach = ((!me.test)&&(!me.ask));

            me.saveas = me.ppp.getProperty("saveas");

            try {

                me.setNN();

                if (me.queAns.length == 0) {
                    if(me.ask){
                        System.err.println("Question not provided with -a switch");
                        System.exit(-1);
                    }

                    me.ask = (me.ppp.getProperty("update") != null);


                    // OPUTPUT TEACHACTION TO A FILE
                    String progress[] = null;
                    PrintStream progressout = null;
                    if(!me.ask && !me.test){
                        String progressvals = me.ppp.getProperty("progressvalues");
                        if(progressvals != null && progressvals.trim().length()!=0){
                            progressvals = "Count " + progressvals;
                            progress = progressvals.split("\\s+");

                            File f = new File(me.nn.getName() + ".progress");
                            try {
                                f.createNewFile();
                                /*if(!f.createNewFile()){
                                    f.delete();
                                    if(!f.createNewFile()){
                                        throw new NeuralException("Couldn't create teaching progress output file " +
                                                f.getName());
                                    }
                                }*/
                            } catch (IOException ex3) {
                                throw new NeuralException("Couldn't create teaching progress output file " +
                                                          f.getName() + "because of an i/o error", ex3);
                            }
                            try {
                                progressout = new PrintStream(new FileOutputStream(f), true);
                                progressout.println(me.nn.getName() +"'s Teaching Progress Data");
                                progressout.println(new java.util.Date());
                                progressout.println("-------------------------");

                                for(int i=0; i<progress.length; i++){
                                    if(i>0)
                                        progressout.print('\t');
                                    progressout.print(progress[i]);
                                }
                                progressout.println();
                            } catch (FileNotFoundException ex2) {
                                throw new NeuralException("Couldn't open teaching progress output file " +
                                                          f.getName() + " - not found", ex2);
                            }

                        }
                    }
                    jdbcprocess(me.nn, me.ppp, me.ask, me.test, System.out, progress, progressout);
                } else {
                    if (me.ask) {
                        askAction(me.nn, me.verbose, me.queAns, System.out);
                    } else {
                        testTeachAction(me.nn, me.verbose, me.queAns, me.test,
                                        System.out);
                    }
                }

                //me.nn.outputDefinition(System.out);

                me.saveAction();
            } catch (NeuralException e) {
                //System.err.println("\nNeural exception: " + e.getMessage());
                e.printStackTrace(System.err);
                System.exit( -1);
            }
        } catch(OutOfMemoryError e){
            e.printStackTrace(System.err);
            System.exit( -1);
        }
    }

    private void setNN(){
        nn = NeuralNetwork.getNeuralNetwork(ppp.getProperty("network"));
        nn.setNeuralEventListener(new NeuralEventAdapter(System.out));
    }

    private void saveAction(){
        // save processed network
        try{

            if (saveas != null) {
                try {
                    nn = nn.getCopy(saveas, false);
                    System.out.println("Saved as " + saveas + '.');
                } catch (NeuralNetworkCreationException ne) {
                    nn = nn.getCopy(saveas, true);
                    System.out.println("File " + saveas + " is overwritten");
                }
            }

            if (nn instanceof NeuralNetworkEditable)
                ((NeuralNetworkEditable) nn).save();

        } catch(ClassCastException e){
            e.printStackTrace();
            NeuralNetworkRemote nnrem = (NeuralNetworkRemote)nn;
            nnrem.disconnect();
        }
    }




    private Properties getConfig(String args[], Vector leftArgs){

        String propsfile = CONFIG;

        Properties props = new Properties();
        int i=0;
        for(i=0; i<args.length; i++){
            if (args[i].charAt(0) == '-') {
                char a = args[i].charAt(1);
                if (args[i].length() == 2) {
                    if (a == 'c') {// config; skip now
                        i++;
                        try{
                            propsfile = args[i];
                        }
                        catch(ArrayIndexOutOfBoundsException e){
                            System.err.println("Config file name after -c switch not specified");
                            this.printHelp(System.err, false);
                            System.exit(-1);
                        }
                        continue;
                    }
                    if (a == 'h') {// help
                        this.printHelp(System.out, true);
                        System.exit(0);
                    }
                    if (a == 'r') {// runs count
                        i++;
                        try{
                            props.setProperty("runs", args[i]);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            System.err.println(
                                    "Runs count after -r switch not specified");
                            this.printHelp(System.err, false);
                            System.exit( -1);
                        }
                        continue;
                    }
                    if (a == 'a') {// answer output
                        props.setProperty("ask", "true");
                        continue;
                    }
                    if (a == 'u') {// answer record
                        props.setProperty("update", "true");
                        continue;
                    }
                    if (a == 'p') {// probe (test)
                        props.setProperty("test", "true");
                        continue;
                    }
                    if (a == 'v') {// verbose
                        props.setProperty("verbose", "true");
                        continue;
                    }
                    if (a == 't') {// target error
                        i++;
                        try{
                            props.setProperty("target", args[i]);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            System.err.println(
                                    "Target minimum error after -t switch not specified");
                            this.printHelp(System.err, false);
                            System.exit( -1);
                        }
                        continue;
                    }
                    if (a == 'n') {// neural network
                        i++;
                        try {
                            props.setProperty("network", args[i]);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            System.err.println(
                                    "Neural Network name after -n switch not specified");
                            this.printHelp(System.err, false);
                            System.exit( -1);
                        }
                        continue ;
                    }
                    if (a == 'S') {// save as
                        i++;
                        try {
                            props.setProperty("saveas", args[i]);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            System.err.println(
                                    "New Neural Network name after -S switch not specified");
                            this.printHelp(System.err, false);
                            System.exit( -1);
                        }
                        continue ;
                    }
                    break;
                } else if (a == 'D') {
                    String[] propval = args[i].substring(2).split("=", 2);
                    props.setProperty(propval[0], propval[1]);
                }
                else
                    break;
            }
            else
                break;
        }
        for (int j = i; j < args.length; j++) {
            leftArgs.add(args[j]);
        }


        Properties retprops = new Properties();
        try {
            FileInputStream prf = new FileInputStream(propsfile);
            retprops.load(prf);
            prf.close();
        } catch (FileNotFoundException ex2) {
            System.err.println("File " + propsfile + " not found");
            System.exit(-1);
        } catch (IOException ex2) {
            System.err.println("File " + propsfile +
                                       " couldn't get open, use " + this.CONFIG);
            System.exit(-1);
        }

        Enumeration enu = props.propertyNames();
        while(enu.hasMoreElements()){
            String prop = (String) enu.nextElement();
            retprops.setProperty(prop, props.getProperty(prop));
        }

        return retprops;
    }

    private static void printHelp(PrintStream out, boolean more){
        if(more){
            out.println(DESCRIPTION);
        }

        out.println("Usage: nnt [-n <name>] [-a] [-u] [-p] [-r N] [-t <double>] [-c <filename>]");
        out.println("           [-S <name>] [-D<configname=value>] [-v] [-h]");
        out.println("           [q1 [q2 [q3] ...] [a1 [a2] ...]]");

        if(more){
            out.println();
            out.println("Options: ");
            out.println("  -n <name>              network: name of the neural network to use instead of");
            out.println("                         that specified in configuration file");
            out.println("  -a                     ask: don't train the network; run the case set once");
            out.println("                         only");
            out.println("  -u                     update: if use JDBC source, don't train the network,");
            out.println("                         update the datasource with the network's responces;");
            out.println("                         run the case set once only");
            out.println("  -p                     probe: compute errors (probe the network) without");
            out.println("                         training; run the case set once only");
            out.println("  -r <N>                 runs: maximum quantity of runs of the training set");
            out.println("                         taken from JDBC datasource; ignored when processing");
            out.println("                         command line arguments, or when probing network, or");
            out.println("                         when updating JDBC set");
            out.println("  -t <double>            target: minimum target error");
            out.println("  -c <filename>          config: nnt configuration file instead of the default");
            out.println("                         " + CONFIG);
            out.println("  -S <name>              save as: after processing, save the network with the");
            out.println("                         specified name");
            out.println("  -D<configname=value>   detail: override the configuration value in the config");
            out.println("                         file (can set values without spaces only)");
            out.println("  -v                     verbose: output the data set with answers and errors,");
            out.println("                         each case in separate line");
            out.println("  -h                     help: output this information");
            out.println();
            out.println("Neural network input and output values can be taken from command line arguments.");
            out.println("The first unrecognized option is considered as the first input value.");
            out.println("If command line values not set, they are taken from JDBC datasource (see");
            out.println(CONFIG+")");
        }
        else{
            out.println("Use -h for help");
        }
    }







    public String actionOutput( boolean verbose){
        StringBuffer sb = new StringBuffer();

        String s = sb.toString();
        return s;
    }



    private static boolean needStopJdbcProcess = false;
    public static void stopJdbcProcess(){
        needStopJdbcProcess = true;
    }

    public static int jdbcprocess(NeuralNetwork nn, Properties ppp, boolean ask, boolean test, PrintStream output, String[] progress, PrintStream progressout){
        long progresscount = 0;
        boolean teach = ((!test)&&(!ask));
        String prop;
        prop = ppp.getProperty("verbose");
        boolean verbose = (prop!=null && prop.trim().length()!=0);
        String[] queAns;

        String url = ppp.getProperty("dburl");
        String tab = ppp.getProperty("dbtable");
        String driv = ppp.getProperty("dbdriver");

        double target;
        long runcnt;
        prop = ppp.getProperty("target");
        if(prop!=null && prop.trim().length()!=0){
            try {
                target = Double.parseDouble(prop);
            } catch (NumberFormatException e) {
                throw new NeuralException("Invalid target minimum error - " +
                                          prop);
            }
        }
        else{
            target=Double.NaN;
        }
        String idflds = ppp.getProperty("dbidfields");
        String idfldsarr[] = null;
        String[] idvals = null;

        if(teach){
            prop = ppp.getProperty("runs");
            if (prop != null && prop.trim().length()!=0){
                runcnt = Long.parseLong(prop);
            } else {
                runcnt = Long.MAX_VALUE;
            }
        }
        else{
            runcnt = 1;
        }

        //output.println(teach + "," + runcnt);

        if(driv==null || driv.trim().length()==0){
            throw new NeuralException("No JDBC driver specified");
        }
        try {
            Class.forName(driv);
        } catch (ClassNotFoundException ex) {
            throw new NeuralException("JDBC driver " + driv +
                                       " couldn't get loaded", ex);
        }

        String sqlselect;
        prop = ppp.getProperty("dbquery");
        if(prop==null || prop.trim().length()==0){
            StringBuffer sb = new StringBuffer(SELECTSQL);
            sb.replace(sb.indexOf(TABLE), sb.indexOf(TABLE)+TABLE.length(), tab);
            sqlselect = sb.toString();
        }
        else{
            sqlselect = prop;
        }

        String[] fnames = nn.getFieldNames();


        String sqlupdate="";
        if(!ask){
            queAns=new String[fnames.length];
        }else{
            queAns = new String[nn.getAskSize()];


            StringBuffer sb = new StringBuffer(UPDATESQL);
            sb.replace(sb.indexOf(TABLE), sb.indexOf(TABLE)+TABLE.length(), tab);

            StringBuffer valcond = new StringBuffer();
            int outstart = nn.getAskSize();  //.getInputNeurons().length;
            for(int i=outstart; i< fnames.length; i++){
                if(i>outstart){
                    valcond.append(',');
                    valcond.append(' ');
                }
                valcond.append(fnames[i]);
                valcond.append('=');
                valcond.append('?');
            }
            sb.replace(sb.indexOf(VALUE), sb.indexOf(VALUE)+VALUE.length(), valcond.toString());

            valcond.setLength(0);
            if(idflds!=null && idflds.trim().length()!=0){
                StringTokenizer st = new StringTokenizer(idflds, ",", false);
                Vector idfldsvect = new Vector();

                int i=0;
                while(st.hasMoreTokens()){
                    if(i>0){
                        valcond.append(" and ");
                    }
                    String f = st.nextToken();
                    valcond.append(f);
                    valcond.append('=');
                    valcond.append('?');
                    idfldsvect.add(f);
                    i++;
                }
                idfldsarr = new String[i];
                idfldsvect.toArray(idfldsarr);
                idvals = new String[idfldsarr.length];
            }
            else{
                for(int i=0; i<outstart; i++){
                    if(i>0){
                        valcond.append(" and ");
                    }
                    valcond.append(fnames[i]);
                    valcond.append('=');
                    valcond.append('?');
                }
                idvals = new String[outstart];
            }
            sb.replace(sb.indexOf(COND), sb.indexOf(COND)+COND.length(), valcond.toString());

            sqlupdate=sb.toString();

        }

        //output.println(sqlselect);
        //output.println(sqlupdate);



        Connection con;
        ResultSet rs;

        double[] errs;

        try {
            // открытие источника данных
            prop = ppp.getProperty("dbuser");
            if(prop != null && prop.trim().length()==0)
                prop = null;
            String prop2 = ppp.getProperty("dbpassword");
            if(prop2 != null && prop2.trim().length()==0)
                prop2=null;
            con = DriverManager.getConnection(url, prop, prop2);
            Statement stmt = con.createStatement(/*ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE*/);

            PreparedStatement upd = null;
            ResultSetMetaData md = null;
            if(ask){
                upd = con.prepareStatement(sqlupdate);
            }

            rs = null;

            boolean gotmd = false;
            int types[][] = new int[2][];
            types[0]=null;// типы полей ответа
            types[1]=null;// типы полей вопроса

            double runAvgMaxErr = 0;// средняя ошибка за прогон.
            double oldRunAvgMaxErr = 0;
            // а это - посчитанное в процессе прогона количество примеров.
            //применяется для расчета  средней ошибки за прогон
            long runMaxErrCnt = 0;

            long starttime = System.currentTimeMillis();

            needStopJdbcProcess = false;
            int k = 0;
runs:       while(true) /*for (k = 0; k < runcnt; k++)*/ { // обучающие прогоны
                k++;

                rs = stmt.executeQuery(sqlselect);

                // find out SQLTypes for result columns for databases that don't take setString for double
                if(ask && !gotmd){// do only if we need to record
                    md = rs.getMetaData();
                    gotmd=true;// mark that we have metadata already
                    if(types[0]==null){
                        types[0] = new int[nn.getAnswerSize()];
                        for(int i=0; i< nn.getAnswerSize(); i++){
                            int col = rs.findColumn(nn.getAnswerFieldName(i));
                            types[0][i] = md.getColumnType(col);
                        }
                    }
                }

                long rowcount=0;
                while (rs.next()) { // проход по обучающим примерам
                    rowcount++;

                    // запись вопроса и ответа в массив
                    for(int j=0;j<queAns.length; j++){
                        queAns[j] = rs.getString(nn.getAskFieldName(j));
                    }

                    // вывод прогресса
                    if(!ask || verbose){
                        String lg = "Run:\t" + k + "   Case:\t" + rowcount +
                                    (nn.getName()!=null? " Net: " + nn.getName():"") +
                                    " ";
                        output.print(lg);
                    }



                    //*****************************
                    //errs = action(nn, verbose, queAns, test, ask, output);
                    String[] answ;

                    /*if (ask) {
                        answ = nn.ask(queAns);
                        //askAction(me.nn, me.verbose, me.queAns, System.out);
                        errs = null;
                    } else {
                        if (test)
                            errs = nn.test(queAns);
                        else
                            errs = nn.teach(queAns);
                        answ = null;
                        //testTeachAction(me.nn, me.verbose, me.queAns, me.test, System.out);
                    }*/


                    if(ask){
                        answ = askAction(nn, verbose, queAns, output);
                        errs = null;
                    } else {
                        errs = testTeachAction(nn, verbose, queAns, test, output);
                        answ = null;
                    }
                    //*****************************


                    if(ask){
                        // запись ответа

                        // запоминание айдишника записи
                        if(idfldsarr!=null){
                            for(int j=0; j<idfldsarr.length; j++){
                                try{
                                    idvals[j] = rs.getString(idfldsarr[j]);
                                }
                                catch(SQLException se){// this section works if rs.getString can not read the column twice
                                    // try to take id value from questions
                                    int fname=-1;
                                    for(int l=0; l<fnames.length; l++){
                                        if(idfldsarr[j].equals(fnames[l])){
                                            fname=l;
                                            break;
                                        }
                                    }
                                    idvals[j] = queAns[fname];
                                }
                            }
                            // find out SQLTypes for id columns for databases that don't take setString for double (MSAccess)
                            if(types[1]==null){
                                types[1] = new int[idfldsarr.length];
                                for(int i=0; i< idfldsarr.length; i++){
                                    int col = rs.findColumn(idfldsarr[i]);
                                    types[1][i] = md.getColumnType(col);
                                }
                            }
                        }else{
                            for(int j=0; j<nn.getAskSize(); j++){
                                idvals[j] = queAns[j];
                            }
                            // find out SQLTypes for id columns for databases that don't take setString for double (MSAccess)
                            if(types[1]==null){
                                types[1] = new int[nn.getAskSize()];
                                for(int i=0; i< types[1].length; i++){
                                    int col = rs.findColumn(nn.getAskFieldName(i));
                                    types[1][i] = md.getColumnType(col);
                                }
                            }
                        }


                        // запись в SET
                        int i=0;
                        while(i<types[0].length){
                            upd.setObject(i+1, answ[i], types[0][i]);
                            //upd.setObject(i+1, queAns[i+types[1].length], types[0][i]);
                            //upd.setString(i+1, answer[i]);
                            i++;
                        }
                        // запись в WHERE
                        for(int j=0; j<idvals.length; j++){
                            i++;
                            upd.setObject(i, idvals[j], types[1][j]);
                            //upd.setString(i, idvals[j]);
                        }
                        upd.execute();
                    }

                    if(!ask){

                        // обработка ошибки: нахождение максимальной и сравнение с таргетом
                        if (!Double.isNaN(target)) {
                            if (runMaxErrCnt == 0) {
                                runAvgMaxErr = Double.MAX_VALUE;
                            } else {
                                double maxerr = Util.getMaxMod(errs);
                                runAvgMaxErr = runAvgMaxErr + maxerr / runMaxErrCnt;

                                output.print("AE: ");
                                output.print(oldRunAvgMaxErr);
                            }
                        }
                    }

                    if(verbose){
                        output.println();
                    }
                    else{
                        output.print('\t');
                        output.print('\t');
                        output.print('\r');
                    }


                    // OPUTPUT TEACHACTION TO A FILE
                    if(progressout!=null){
                        progresscount++;

                        progressout.print(progresscount);
                        for(int i=1; i<progress.length; i++){
                            progressout.print('\t');
                            progressout.print(Util.AttributeGetter.getDoubleAttribute(progress[i], nn, null));
                        }
                        progressout.println();
                        progressout.flush();
                    }

                    if(needStopJdbcProcess)
                        break runs;
                }

                // после прогона набора примеров - обработка таргета и выход, если попали
                if (!Double.isNaN(target)) {
                    if(runMaxErrCnt==0)
                        runMaxErrCnt = rowcount;
                    else{
                        if (runAvgMaxErr <= target) {
                            output.println("\nTarget " + target + " met.");
                            break runs;
                        }
                        oldRunAvgMaxErr = runAvgMaxErr;
                        runAvgMaxErr = 0;
                    }
                }

                nn.nextEpoch();
                if(k >= runcnt || needStopJdbcProcess)
                    break runs;
                // сменилась эпоха - надо уведомить учителя
                if(verbose){
                    output.println("------------------------------");
                    output.println("New epoch");
                }
            }
            if(ask)
                upd.close();
            rs.close();
            stmt.close();
            con.close();


            if(progressout!=null)
                progressout.close();

            long endtime = System.currentTimeMillis();
            long worktime = endtime-starttime;
            long seconds = worktime / 1000;
            long minutes = seconds / 60;
            seconds = seconds - minutes * 60;
            long hours = minutes / 60;
            minutes = minutes - hours * 60;

            //logfile.close();
            output.println();
            output.print("Case set run ");
            output.print(k);
            output.print(" times. It took ");
            if(hours>0){
                output.print(hours);
                if(hours - hours/10*10 == 1)
                    output.print(" hour, ");
                else
                    output.print(" hours, ");
            }
            output.print(minutes);
            if(minutes - minutes/10*10 == 1)
                output.print(" minute and ");
            else
                output.print(" minutes and ");
            output.print(seconds);
            if(seconds - seconds/10*10 == 1)
                output.print(" second.");
            else
                output.print(" seconds.");

            return k;
        }
        /*catch (NullPointerException ne){
            throw new NeuralException("NullPointer", ne);
        }*/
        catch (SQLException ex1) {
            throw new NeuralException("JDBC exception occurred - " + ex1.getMessage(), ex1);
        }
    }

    private static double[] testTeachAction(NeuralNetwork nn, boolean verbose, String[] queAns, boolean test, PrintStream output){
        double[] er;

        if(verbose){
            output.print("QA: ");
            output.print(Util.prtArr(queAns, true));
        }
        if(test){
            er = nn.test(queAns);
        }
        else{
            er = nn.teach(queAns);
        }

        if (verbose) {
            String a[] = new String[nn .getAnswerSize()];
            System.arraycopy(queAns, queAns.length-a.length, a, 0, a.length);
            output.print("\tA: ");
            output.print(Util.prtArr(a, true));
        }

        output.print("\tE: ");
        output.print(Util.prtArr(er, true));

        return er;

    }
    private static String[] askAction(NeuralNetwork nn, boolean verbose, String[] queAns, PrintStream output){
        String[] answ;
        if(verbose){
            output.print("Q: ");
            output.print(Util.prtArr(queAns, true));
        }
        answ = nn.ask(queAns);
        if (verbose) {
            output.print("\tA: ");
        }
        output.print(Util.prtArr(answ, verbose));
        return answ;
    }

    class NeuralEventAdapter implements NeuralEventListener{
        PrintStream out;
        NeuralEventAdapter (PrintStream out){
            this.out=out;
        }

        public void neuralEventHappened(NeuralEvent e){
            if (e.getEventType() == e.GENERIC) {
                out.println();
                out.print(e.getSource().getClass().getName());
                out.print(':');
                out.print(' ');
                out.println(e.getMeggage());
            }
        }
    }

}
