package edu.nsp.banking;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.concurrent.ThreadLocalRandom;

public class BankingApp extends JFrame {
    private int customerId;
    private String customerName;
    private long accountId;
    private String accountNumber;
    private JLabel balanceLabel;

    public BankingApp() {
        setTitle("Internet Banking & Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(780, 520);
        setLocationRelativeTo(null);
        showLogin();
    }

    private void showLogin() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(25, 35, 25, 35));
        panel.setBackground(new Color(242, 246, 252));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8); c.fill = GridBagConstraints.HORIZONTAL;
        JLabel heading = new JLabel("Internet Banking System", SwingConstants.CENTER);
        heading.setFont(new Font("SansSerif", Font.BOLD, 25));
        JTextField email = new JTextField(22);
        JPasswordField password = new JPasswordField(22);
        JButton login = new JButton("Login"), register = new JButton("Create account");
        c.gridx=0;c.gridy=0;c.gridwidth=2;panel.add(heading,c);
        c.gridwidth=1;c.gridy++;panel.add(new JLabel("Email"),c);c.gridx=1;panel.add(email,c);
        c.gridx=0;c.gridy++;panel.add(new JLabel("Password"),c);c.gridx=1;panel.add(password,c);
        c.gridx=0;c.gridy++;panel.add(login,c);c.gridx=1;panel.add(register,c);
        login.addActionListener(e -> authenticate(email.getText().trim(), new String(password.getPassword())));
        register.addActionListener(e -> register());
        setContentPane(panel); revalidate();
    }

    private void authenticate(String email, String password) {
        String sql = "SELECT customer_id,full_name FROM customers WHERE email=? AND password_hash=?";
        try (Connection con=DB.getConnection(); PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setString(1,email); ps.setString(2,PasswordUtil.hash(password));
            try (ResultSet rs=ps.executeQuery()) {
                if (!rs.next()) { message("Invalid email or password."); return; }
                customerId=rs.getInt(1); customerName=rs.getString(2);
            }
            loadAccount();
            showDashboard();
        } catch (SQLException ex) { error(ex); }
    }

    private void register() {
        JTextField name=new JTextField(), email=new JTextField();
        JPasswordField pass=new JPasswordField();
        Object[] fields={"Full name",name,"Email",email,"Password",pass};
        if (JOptionPane.showConfirmDialog(this,fields,"New customer registration",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION) return;
        if(name.getText().isBlank() || email.getText().isBlank() || pass.getPassword().length<6) {
            message("Enter name, email and a password of at least 6 characters."); return;
        }
        String account=String.format("%012d", ThreadLocalRandom.current().nextLong(1_000_000_000L, 9_999_999_999L));
        String insert="INSERT INTO customers(full_name,email,password_hash) VALUES(?,?,?)";
        try(Connection con=DB.getConnection()) {
            con.setAutoCommit(false);
            try(PreparedStatement ps=con.prepareStatement(insert,Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1,name.getText().trim()); ps.setString(2,email.getText().trim());
                ps.setString(3,PasswordUtil.hash(new String(pass.getPassword()))); ps.executeUpdate();
                try(ResultSet keys=ps.getGeneratedKeys()) {
                    keys.next(); int id=keys.getInt(1);
                    try(PreparedStatement a=con.prepareStatement("INSERT INTO accounts(customer_id,account_number,balance) VALUES(?,?,0.00)")) {
                        a.setInt(1,id); a.setString(2,account); a.executeUpdate();
                    }
                }
            }
            con.commit(); message("Registration successful. Your account number is "+account+"\nPlease log in.");
        } catch(SQLException ex) { error(ex); }
    }

    private void loadAccount() throws SQLException {
        try(Connection con=DB.getConnection(); PreparedStatement ps=con.prepareStatement(
                "SELECT account_id,account_number FROM accounts WHERE customer_id=?")) {
            ps.setInt(1,customerId);
            try(ResultSet rs=ps.executeQuery()) {
                if(!rs.next()) throw new SQLException("No bank account is linked to this customer.");
                accountId=rs.getLong(1); accountNumber=rs.getString(2);
            }
        }
    }

    private void showDashboard() {
        JPanel main=new JPanel(new BorderLayout(12,12)); main.setBorder(new EmptyBorder(18,20,18,20));
        JPanel top=new JPanel(new BorderLayout());
        JLabel welcome=new JLabel("Welcome, "+customerName+"  |  A/C: "+accountNumber);
        welcome.setFont(new Font("SansSerif",Font.BOLD,16));
        balanceLabel=new JLabel(); balanceLabel.setFont(new Font("SansSerif",Font.BOLD,22));
        top.add(welcome,BorderLayout.NORTH); top.add(balanceLabel,BorderLayout.SOUTH);
        main.add(top,BorderLayout.NORTH);
        JPanel actions=new JPanel(new GridLayout(1,4,10,10));
        JButton deposit=new JButton("Deposit"), withdraw=new JButton("Withdraw"), transfer=new JButton("Transfer"), history=new JButton("Transactions");
        actions.add(deposit);actions.add(withdraw);actions.add(transfer);actions.add(history);
        main.add(actions,BorderLayout.CENTER);
        JButton logout=new JButton("Logout"); logout.addActionListener(e->showLogin());
        main.add(logout,BorderLayout.SOUTH);
        deposit.addActionListener(e->amountAction("DEPOSIT"));
        withdraw.addActionListener(e->amountAction("WITHDRAWAL"));
        transfer.addActionListener(e->transferAction());
        history.addActionListener(e->showHistory());
        setContentPane(main); refreshBalance(); revalidate(); repaint();
    }

    private void refreshBalance() {
        try(Connection con=DB.getConnection();PreparedStatement ps=con.prepareStatement("SELECT balance FROM accounts WHERE account_id=?")) {
            ps.setLong(1,accountId);try(ResultSet rs=ps.executeQuery()){if(rs.next())balanceLabel.setText("Available balance: ₹ "+rs.getBigDecimal(1));}
        } catch(SQLException ex){error(ex);}
    }

    private BigDecimal askAmount() {
        String s=JOptionPane.showInputDialog(this,"Enter amount (₹):");
        if(s==null)return null;
        try { BigDecimal a=new BigDecimal(s.trim()); if(a.compareTo(BigDecimal.ZERO)<=0)throw new NumberFormatException(); return a.setScale(2,java.math.RoundingMode.HALF_UP); }
        catch(NumberFormatException ex){message("Enter a valid amount greater than zero.");return null;}
    }

    private void amountAction(String type) {
        BigDecimal amount=askAmount(); if(amount==null)return;
        try(Connection con=DB.getConnection()) {
            con.setAutoCommit(false);
            try(PreparedStatement lock=con.prepareStatement("SELECT balance FROM accounts WHERE account_id=? FOR UPDATE")) {
                lock.setLong(1,accountId);
                try(ResultSet rs=lock.executeQuery()) {
                    if(!rs.next())throw new SQLException("Account not found.");
                    BigDecimal balance=rs.getBigDecimal(1);
                    if(type.equals("WITHDRAWAL") && balance.compareTo(amount)<0)throw new SQLException("Insufficient balance.");
                }
            }
            String update=type.equals("DEPOSIT")?"UPDATE accounts SET balance=balance+? WHERE account_id=?":"UPDATE accounts SET balance=balance-? WHERE account_id=?";
            try(PreparedStatement ps=con.prepareStatement(update)){ps.setBigDecimal(1,amount);ps.setLong(2,accountId);ps.executeUpdate();}
            try(PreparedStatement ps=con.prepareStatement("INSERT INTO transactions(from_account,to_account,transaction_type,amount,description) VALUES(?,?,?,?,?)")) {
                ps.setObject(1,type.equals("WITHDRAWAL")?accountId:null);ps.setObject(2,type.equals("DEPOSIT")?accountId:null);
                ps.setString(3,type);ps.setBigDecimal(4,amount);ps.setString(5,type+" by customer");ps.executeUpdate();
            }
            con.commit();refreshBalance();message(type+" successful.");
        } catch(SQLException ex){error(ex);}
    }

    private void transferAction() {
        JTextField target=new JTextField(); BigDecimal amount=askAmount(); if(amount==null)return;
        if(JOptionPane.showConfirmDialog(this,target,"Enter beneficiary account number",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        String sql="SELECT account_id FROM accounts WHERE account_number=?";
        try(Connection con=DB.getConnection()) {
            con.setAutoCommit(false); long targetId;
            try(PreparedStatement ps=con.prepareStatement(sql)){ps.setString(1,target.getText().trim());try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new SQLException("Beneficiary account not found.");targetId=rs.getLong(1);}}
            if(targetId==accountId)throw new SQLException("You cannot transfer to the same account.");
            long first=Math.min(accountId,targetId), second=Math.max(accountId,targetId);
            for(long id:new long[]{first,second})try(PreparedStatement ps=con.prepareStatement("SELECT balance FROM accounts WHERE account_id=? FOR UPDATE")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new SQLException("Account not found.");if(id==accountId&&rs.getBigDecimal(1).compareTo(amount)<0)throw new SQLException("Insufficient balance.");}}
            try(PreparedStatement ps=con.prepareStatement("UPDATE accounts SET balance=balance-? WHERE account_id=?")){ps.setBigDecimal(1,amount);ps.setLong(2,accountId);ps.executeUpdate();}
            try(PreparedStatement ps=con.prepareStatement("UPDATE accounts SET balance=balance+? WHERE account_id=?")){ps.setBigDecimal(1,amount);ps.setLong(2,targetId);ps.executeUpdate();}
            try(PreparedStatement ps=con.prepareStatement("INSERT INTO transactions(from_account,to_account,transaction_type,amount,description) VALUES(?,?,'TRANSFER',?,'Account transfer')")){ps.setLong(1,accountId);ps.setLong(2,targetId);ps.setBigDecimal(3,amount);ps.executeUpdate();}
            con.commit();refreshBalance();message("Transfer successful.");
        }catch(SQLException ex){error(ex);}
    }

    private void showHistory() {
        String sql="SELECT transaction_id,transaction_type,amount,description,transaction_time FROM transactions WHERE from_account=? OR to_account=? ORDER BY transaction_time DESC";
        DefaultTableModel model=new DefaultTableModel(new String[]{"ID","Type","Amount (₹)","Description","Date / time"},0);
        try(Connection con=DB.getConnection();PreparedStatement ps=con.prepareStatement(sql)){
            ps.setLong(1,accountId);ps.setLong(2,accountId);
            try(ResultSet rs=ps.executeQuery()){while(rs.next())model.addRow(new Object[]{rs.getLong(1),rs.getString(2),rs.getBigDecimal(3),rs.getString(4),rs.getTimestamp(5)});}
            JTable table=new JTable(model);JScrollPane pane=new JScrollPane(table);pane.setPreferredSize(new Dimension(650,280));
            JOptionPane.showMessageDialog(this,pane,"Transaction history",JOptionPane.INFORMATION_MESSAGE);
        }catch(SQLException ex){error(ex);}
    }

    private void message(String s){JOptionPane.showMessageDialog(this,s);}
    private void error(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
    public static void main(String[] args){SwingUtilities.invokeLater(()->new BankingApp().setVisible(true));}
}
