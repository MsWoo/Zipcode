package zipcode;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;

import utils.DBConn;

public class ZipSearchFrame extends JFrame {

	private JPanel contentPane;
	private JPanel panel;
	private JTextField textField;
	private JScrollPane scrollPane;
	private JTable jlistAddress;
	private JPanel statusPanel;
	private JButton btnConnect;
	private JLabel lblConnStatus;

	private Connection conn = null;
	
	private ButtonGroup btnGroup = new ButtonGroup();
	private JRadioButton jrbeub;
	private JRadioButton jrbdoro;
	private JButton btnSearch;
	private int radioflag = 0; // 0 : 읍면동, 1 : 도로명

	
	public ZipSearchFrame() {
		initialize();
	}
	
	void initialize() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 932, 928);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		contentPane.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		jrbeub = new JRadioButton("읍/면/동     ");
		jrbeub.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				radioflag=0;
			}
		});
		jrbeub.setSelected(true);
		btnGroup.add(jrbeub);
		panel.add(jrbeub);
		
		jrbdoro = new JRadioButton("도로명주소     ");
		jrbdoro.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				radioflag=1;
			}
		});
		btnGroup.add(jrbdoro);
		panel.add(jrbdoro);
		
		
		textField = new JTextField();
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getZipcode();
			}
		});
		panel.add(textField);
		textField.setColumns(30);
		
		btnSearch = new JButton("     Search     ");
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getZipcode();
			}
		});
		panel.add(btnSearch);

		scrollPane = new JScrollPane();
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		jlistAddress = new JTable();
		jlistAddress.setRowSelectionAllowed(false);
		scrollPane.setViewportView(jlistAddress);
		
		statusPanel = new JPanel();
		contentPane.add(statusPanel, BorderLayout.SOUTH);
		statusPanel.setLayout(new BorderLayout(0, 0));
		
		btnConnect = new JButton("     Connect      ");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				connDBAction();
			}
		});
		statusPanel.add(btnConnect, BorderLayout.WEST);
		
		lblConnStatus = new JLabel("");
		statusPanel.add(lblConnStatus, BorderLayout.CENTER);
	}

	protected void connDBAction() {
		try {
			lblConnStatus.setText("");
			DBConn dbconn = new DBConn();
			conn = dbconn.getConnection();
			String connMsg = " Connected to "
					+ dbconn.getServerIP() + " "
					+ dbconn.getDBname();
			lblConnStatus.setText(connMsg);
			
		} catch (ClassNotFoundException e) {
			lblConnStatus.setText("ClassNotFoundException");
			e.printStackTrace();
		} catch (SQLException e) {
			lblConnStatus.setText("SQLException");
			e.printStackTrace();
		}
	}

	private void getZipcode(){
		DefaultTableModel addrTableModel = new DefaultTableModel();
		Vector<String> colNames = new Vector<String>();
		colNames.add("Seq.");
		colNames.add("== 우편번호 ==");
		colNames.add("== 도로명 ==");
		colNames.add("== 지번 ==");
		colNames.add("== 건물명 ==");
		int colMax = 5;
		
		Vector<? extends Vector> rowDataList = new Vector<>();
		
		addrTableModel.setDataVector(rowDataList, colNames);		
		jlistAddress.setModel(addrTableModel);
		jlistAddress.getColumnModel().getColumn(0).setPreferredWidth(50);
		jlistAddress.getColumnModel().getColumn(1).setPreferredWidth(50);
		jlistAddress.getColumnModel().getColumn(2).setPreferredWidth(200);
		jlistAddress.getColumnModel().getColumn(3).setPreferredWidth(200);
		jlistAddress.getColumnModel().getColumn(4).setPreferredWidth(50);
		jlistAddress.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		
		String inputCode = textField.getText();
		
		String input1 = null;
		String input2 = null;
		String input3 = null;
		String input4 = null;
		
		int blank = countBlank(inputCode);
		
		if(blank == 2) {
			input1 = inputCode.split(" ")[0];
			input2 = inputCode.split(" ")[1];
			input3 = inputCode.split(" ")[2];
		}
		else if(blank == 3) {
			input1 = inputCode.split(" ")[0];
			input2 = inputCode.split(" ")[1];
			input3 = inputCode.split(" ")[2];
			input4 = inputCode.split(" ")[3];
			
			input2 = input2 + " " + input3;
			input3 = input4;
		}
		else {//blank == 1, 세종시
			input1 = inputCode.split(" ")[0];
			input2 = inputCode.split(" ")[1];
		}
		
		String SQL;
		if(radioflag == 0){//읍,면,동

			if(blank == 1) {//세종시
				SQL = "SELECT zipcode, sido, sigungu, doro, dong_hj, buildno1, buildno2, jibun1, jibun2, buildname "
				+ "FROM zipcode "
				+ "WHERE sido = ? and dong_hj = ? ";
			}
			else {
				SQL = "SELECT zipcode, sido, sigungu, doro, dong_hj, buildno1, buildno2, jibun1, jibun2, buildname "
				+ "FROM zipcode "
				+ "WHERE sido = ? and sigungu = ? and dong_hj = ? ";
			}

		}
		else{//radioflag==1 도로명
			if(blank == 1) {//세종시
				SQL = "SELECT zipcode, sido, sigungu, doro, dong_hj, buildno1, buildno2, jibun1, jibun2, buildname "
				+ "FROM zipcode "
				+ "WHERE sido = ? and doro = ? ";
			}
			else {
				SQL = "SELECT zipcode, sido, sigungu, doro, dong_hj, buildno1, buildno2, jibun1, jibun2, buildname "
				+ "FROM zipcode "
				+ "WHERE sido = ? and sigungu = ? and doro = ? ";
			}
		}
		
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(SQL);
			
			if(blank == 1) {//세종시
				pstmt.setString(1, input1);
				pstmt.setString(2, input2);
			}
			else {
				pstmt.setString(1, input1);
				pstmt.setString(2, input2); 
				pstmt.setString(3, input3); 
			}
            ResultSet rs = pstmt.executeQuery();
            
            int seqNo = 0;
            while(rs.next()) {
            	String zipcode = rs.getString("zipcode");
            	
            	String addrLine = String.format("%s", zipcode);
            	String sido = rs.getString("sido");
            	String sigungu = rs.getString("sigungu");
            	String doro = rs.getString("doro");
            	String dong = rs.getString("dong_hj");
            	String doro1 = rs.getString("buildno1");
            	String doro2 = rs.getString("buildno2");
            	String jibun1 = rs.getString("jibun1");
            	String jibun2 = rs.getString("jibun2");
            	String buildname = rs.getString("buildname");
            	            
            	String[] rowData = new String[colMax];
            	seqNo++;
            	rowData[0] = String.valueOf(seqNo);
            	rowData[1] = addrLine;
            	
            	String preInput = sido +" "+ sigungu;
            	
            	doro1 = preInput +" "+ doro +" "+ doro1;
            	jibun1 = preInput +" "+ dong +" "+ jibun1;
            	
            	if(doro2.equals("0"))
            		rowData[2] = doro1;
            	else
            		rowData[2] = doro1+"-"+doro2;
            	if(jibun2.equals("0"))
            		rowData[3] = jibun1;
            	else
            		rowData[3] = jibun1+"-"+jibun2;
            	rowData[4] = buildname;
            	addrTableModel.addRow(rowData);
            }
            System.out.println("Selected length="+rowDataList.size());
            
        } catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (pstmt!=null) pstmt.close();
				if (conn!=null) conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}	
        this.validate();
	}
	
	public int countBlank(String str) {
		int count = 0;
		for(int i=0;i<str.length();i++) {
			if(str.charAt(i) == ' ')
				count++;
		}
		return count;
	}

}
