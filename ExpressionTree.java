import java.io.*;
import java.util.*;
import java.text.*;
import java.math.*;
import java.util.regex.*;

import javafx.scene.Parent;
class Node {
    String expression;
    List<Node> children;
    Node parent;
    int parentStartIdx;
    boolean enclosed = false;
    boolean firstNode = false;
    Node(String expression){
        this.expression = expression;
        children = new ArrayList<Node>();
        parent = null;
    }
    Node(String expression, boolean enclosed){
        this.expression = expression;
        children = new ArrayList<Node>();
        this.enclosed = enclosed;
        parent = null;
    }
}
public class ExpressionTree {
	public String evaluate(String in){
		boolean simplified = false;
		String res = "";
		// eliminate all white spaces
		in = in.replaceAll(" ", "");
		if(in.length() == 0){
			return "";
		}
		System.out.println("The input string is: " + in);
		// track the expression operator
		int cnt = in.length() - 1;
		while(cnt != 0 && in.charAt(cnt) != '/'){
			cnt --;
		}
		String op = in.substring(cnt + 1);
        if(op.length() == 0 && in.charAt(cnt) == '/'){
            return in.substring(0, cnt);
        }
        if(cnt == in.length() - 1){
            return in;
        }
		in = in.substring(0, cnt);
		// construct the expression tree
		Node root = constructTree(in);
		// perform the operation
		boolean reversed = false;
		int count = 0;
		for(int pre = 0; pre < op.length(); pre ++){
			if(op.charAt(pre) == 'R'){
				count ++;
			} 
		}
		for(int i = 0; i < op.length(); i++){
			if(op.charAt(i) == 'S'){
				if(!simplified){
					simplified = true;
					res = simplify(root);
					reversed = true;
				}
			}
			if(op.charAt(i) == 'R'){
					root = reverse(root);
					res = root.expression;
			}
		}
		return res;
	}

	private Node constructTree(String in){
		Node root = new Node(in);
			// construct the tree iteratively
				Stack<Node> stack = new Stack<>();
				stack.push(root);
				while(!stack.isEmpty()){
					Node currNode = stack.pop();
					String curr = currNode.expression;
					int idx = 0;
					while(idx < curr.length()){
						String tmp = "";
						int startIdx = idx;
						if(Character.isLetter(curr.charAt(idx))){
							while(idx < curr.length() && Character.isLetter(curr.charAt(idx))){
								tmp += curr.charAt(idx);
								idx++;
							}
							// add a new node to the children
							Node newNode = new Node(tmp);
							// set the start index of the parent
							newNode.parentStartIdx = startIdx;
							// set the parent of the new node
							newNode.parent = currNode;
							// mark if this is the first node
							if(currNode.children.size() == 0){
								newNode.firstNode = true;
							}
							currNode.children.add(newNode);
						}
						else{
							if(curr.charAt(idx) == '('){
								int startParenthesisIdx = idx;
								// record at which index does the parent starts
								int start = idx;
								int open = 1;
								boolean first = true;
								while(open != 0 && idx < curr.length()){
									tmp += curr.charAt(idx);
									if(!first && curr.charAt(idx) == '('){
										open ++;
									}
									first = false;
									if(curr.charAt(idx) == ')'){
										open --;
									}
									idx++;
								}
								tmp = tmp.substring(1, tmp.length() - 1);
								Node child = new Node(tmp, true);
								// set the starting index
								child.parentStartIdx = startParenthesisIdx;
								// add the parent node to child
								child.parent = currNode;
								// mark if this is the first node
								if(currNode.children.size() == 0){
									child.firstNode = true;
								}
								currNode.children.add(child);
								stack.push(child);
							}
						}	
					}
				}
		return root;
	}
	private String getReversedString(Node root){
		String res = "";
		for(int i = 0; i < root.children.size(); i++){
			res += root.children.get(i).expression;
		}
		return res;
	}
	private Node reverse(Node root){
		String res = reverseString(root);
		Node newNode = constructTree(res);
		return newNode;
	}

	private String reverseString(Node root){
		String res = "";
		if(root.children.size() == 0){
			return res;
		}
		// perform a reverse based on the child list
		for(int i = root.children.size() - 1; i >= 0; i--){		
			String curr = root.children.get(i).expression;
			if(root.children.get(i).enclosed == true){
				String newString = "(";
				newString += curr;
				newString += ")";
				curr = newString;
			}
			for(int j = curr.length() - 1; j >= 0; j--){
				if(curr.charAt(j) == '('){
					res += ')';
				}
				else if(curr.charAt(j) == ')'){
					res += '(';
				}
				else{
					res += curr.charAt(j);
				}
			}
		}
		return res;
	}
	private String simplify(Node root){
		String res = "";
		Stack<Node> stack = new Stack<>();
		stack.push(root);
		while(!stack.isEmpty()){
			Node curr = stack.pop();
			// remove the parenthesis and track back to its parent
			if(curr.firstNode == true && curr.enclosed == true && curr.parent != null){
				Node ptr = curr;
				// starting with the parent at the immediate level
				int start = ptr.parentStartIdx, end = ptr.parentStartIdx + 1 + ptr.expression.length();
				while(ptr.parent.parent != null){
					ptr = ptr.parent;
					start += ptr.parentStartIdx + 1;
					end = start + 1 + curr.expression.length();
				}
				curr.enclosed = false;
				// get the parent pointer string
				String parentString = ptr.parent.expression;
				// construct a new string with string builder
				StringBuilder sb = new StringBuilder();
				sb.append(parentString.substring(0, start));
				sb.append(" ");
				sb.append(parentString.substring(start + 1, end));
				sb.append(" ");
				sb.append(parentString.substring(end + 1));
				ptr.parent.expression = sb.toString();
			}
			res = root.expression.replaceAll(" ", "");
			
			
			
			for(int i = curr.children.size() - 1; i >= 0; i--){
				
				stack.push(curr.children.get(i));
				
			}
		}	
		return res;
	}

	public static void main(String [] args){
		ExpressionTree et = new ExpressionTree();
		System.out.println("Start testing... ");
		System.out.println("Test 1 ... ");
		String res = et.evaluate("(AB)C/");
		System.out.println(res);
		
		System.out.println("Test 2 ... ");
		res = et.evaluate("(AB)C/S");
		System.out.println(res);
		
		System.out.println("Test 3 ... ");
		res = et.evaluate("(AB)C/RS");
		System.out.println(res);
		
		System.out.println("Test 4 ... ");
		res = et.evaluate("A(BC)/RS");
		System.out.println(res);
		
		System.out.println("Test 5 ... ");
		res = et.evaluate("A(BC)/RSR");
		System.out.println(res);
	}
}
