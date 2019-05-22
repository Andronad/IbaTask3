import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


class Manager{
    public int numberEmpty;
    public int numberPart;
    public int numberOpen;
    public int totalSum;
    public boolean log;
    public Manager(boolean l){
        log=l;
    }
    public ArrayList<String> readFile(String name,String type) throws IOException, InterruptedException {
        File file = new File(name);
        BufferedReader br = new BufferedReader(new FileReader(file));
        ArrayList<String> result=new ArrayList<>();
        String st;
        while ((st = br.readLine()) != null) {
            Date date=new Date();
            SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
            boolean cor=true;
            if("inv".equals(type)){
                cor=this.checkCorrectInv(st);
            }
            else{
                cor=this.checkCorrectPay(st);
            }
            result.add(st+" "+formatForDateNow.format(date));
        }
        return result;
    }
    public boolean checkCorrectInv(String st){
        String[] all=st.split(" ");
        if(all.length!=2) return false;

    }
    public void clearMaster(String file) throws IOException {
        Files.write(Paths.get(file), "".getBytes());
    }
    public void writeMaster(String inv,ArrayList<String> pays,String file) throws InterruptedException, IOException {
        String[] invs=inv.split(" ");
        int sum=Integer.parseInt(invs[1]);
        int curSum=this.getCurrentBalance(sum,pays);
        if(this.log){
            this.changeStat(sum,curSum);
        }
        String out="I \t"+invs[0]+" \t"+invs[2]+" \t"+sum+" \t"+curSum;
        Files.write(Paths.get(file), (out+"\n").getBytes(), StandardOpenOption.APPEND);
        for (int j = 0; j < pays.size(); j++) {
            String[] temp=pays.get(j).split(" ");
            sum-=Integer.parseInt(temp[1]);
            out="P \t"+temp[0]+" \t"+temp[3]+" \t"+temp[1]+" \t"+sum;
            Files.write(Paths.get(file), (out+"\n").getBytes(), StandardOpenOption.APPEND);
        }
        Files.write(Paths.get(file), "----------------------\n".getBytes(), StandardOpenOption.APPEND);
    }
    public void writeMasterJustInv(ArrayList<String> invs,String file) throws IOException, InterruptedException {
        Files.write(Paths.get(file), "just invoices\n".getBytes(), StandardOpenOption.APPEND);
        for (int i = 0; i < invs.size(); i++) {
            String[] inv=invs.get(i).split(" ");
            this.changeStat(Integer.parseInt(inv[1]),Integer.parseInt(inv[1]));
            String out="I \t"+inv[0]+" \t"+inv[2]+" \t"+inv[1]+" \t"+inv[1];
            Files.write(Paths.get(file), (out+"\n").getBytes(), StandardOpenOption.APPEND);
        }
        Files.write(Paths.get(file), "----------------------\n".getBytes(), StandardOpenOption.APPEND);
    }
    public void writeMasterStat(String file) throws IOException {
        if(this.log){
            String inf="Number of settled invoices = "+this.numberEmpty+
                    "\nNumber of partially settled invoices = "+this.numberPart+
                    "\nNumber of open invoices = "+this.numberOpen+
                    "\nTotal amount = "+this.totalSum+"\n";
            Files.write(Paths.get(file), inf.getBytes(), StandardOpenOption.APPEND);
        }
    }
    private int getCurrentBalance(int sum,ArrayList<String> pays){
        for (int i = 0; i < pays.size(); i++) {
            Integer pay=Integer.parseInt(pays.get(i).split(" ")[1]);
            sum-=pay;
        }
        return sum;
    }
    public void changeStat(int sum,int cur){
        if(cur==0){
            this.numberEmpty++;
        }
        else{
            if(sum==cur){
                this.numberOpen++;
            }
            else{
                this.numberPart++;
            }
        }
        totalSum+=cur;
    }
}
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        boolean log=false;
        if (args.length!=0&&"-log".equals(args[0])){
            log=true;
        }
        Manager m=new Manager(log);
        m.clearMaster("MASTER.txt");
        ArrayList<String> invoices=m.readFile("INVOICE.txt","inv");
        ArrayList<String> payments=m.readFile("PAYMENT.txt","pay");
        ArrayList<String> invWithoutPay=new ArrayList<>();
        for (int i = 0; i < invoices.size(); i++) {
            String inv=invoices.get(i);
            String[] invs=inv.split(" ");
            Boolean flag=false;
            ArrayList<String> payForInv=new ArrayList<>();
            for (int j = 0; j < payments.size(); j++) {
                String pay=payments.get(j);
                String[] pays=pay.split(" ");
                if(invs[0].equals(pays[2])){
                    flag=true;
                    payForInv.add(pay);
                }
            }
            if(flag){
                m.writeMaster(inv,payForInv,"MASTER.txt");
            }
            else{
                invWithoutPay.add(inv);
            }
        }
        m.writeMasterJustInv(invWithoutPay,"MASTER.txt");
        m.writeMasterStat("MASTER.txt");
    }
}
