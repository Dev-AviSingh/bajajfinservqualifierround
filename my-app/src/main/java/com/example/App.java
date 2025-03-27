package com.example;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Scanner; 

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class CredsNotFound extends Exception {
    public CredsNotFound(String m) {
        super(m);
    }
}

class StudentInfo{
    public String firstName;
    public String rollNumber;

    public StudentInfo(String firstName, String rollNumber){
        this.firstName = firstName;
        this.rollNumber = rollNumber;
    }
}

public class App 
{
    public StudentInfo extractStudentInfo(JSONObject jsonObject) throws CredsNotFound{
        if (!jsonObject.has("student")) {
            throw new CredsNotFound("'student' object does not exist.");
        }

        JSONObject studentObject = jsonObject.getJSONObject("student");

        // Check if 'first_name' and 'roll_number' exist
        if (!studentObject.has("first_name") || !studentObject.has("roll_number")) {
            throw new CredsNotFound("Either 'first_name' or 'roll_number' does not exist.");
        }

        // Extract 'first_name'
        String firstName = studentObject.optString("first_name", null);

        // Extract 'roll_number' and convert to string if necessary
        Object rollNumberObj = studentObject.get("roll_number");
        String rollNumber;
        if (rollNumberObj instanceof String) {
            rollNumber = (String) rollNumberObj;
        } else if (rollNumberObj instanceof Number) {
            rollNumber = rollNumberObj.toString();
        } else {
            throw new CredsNotFound("'roll_number' is neither a string nor a number.");
        }

        System.out.println("First Name: " + firstName);
        System.out.println("Roll Number: " + rollNumber);

        StudentInfo studentInfo = new StudentInfo(firstName, rollNumber);
        
        return studentInfo;
    }

    public void dumpStringToFile(String filePath, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }

    JSONObject readJsonFromFile(String filename){
        String data = "";
        try {
            File myObj = new File(filename);
            Scanner myReader = new Scanner(myObj);
            data = "";
            while (myReader.hasNextLine()) {
                data += myReader.nextLine();
            }
            myReader.close();
            } catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
        }
        // Parse JSON string
        JSONObject jsonObject = new JSONObject(data);
        return jsonObject;
    }

    String generateMD5HashFromCredentials(String first_name, String roll_number){
        String resultingString = first_name + roll_number;
        byte[] bytesOfMessage = null;
        try{
            bytesOfMessage = resultingString.getBytes("UTF-8");
        }catch(UnsupportedEncodingException e){
            System.out.println("The UTF-8 encoding does not exist on this particular device. Please install it.");
            System.exit(1);
        }

        MessageDigest md = null;
        try{
            md = MessageDigest.getInstance("MD5");
        }catch(NoSuchAlgorithmException e){
            System.out.println("The MD5 algorithm does not exist on this particular device. Please install it.");
            System.exit(1);
        }

        byte[] messageDigest = md.digest(bytesOfMessage);

        BigInteger no = new BigInteger(1, messageDigest);
 
        // Convert message digest into hex value
        String hashtext = no.toString(16);
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }

    public static void main( String[] args )
    {
        App app = new App();
        String inputFileName = "input.json";
        String outputFileName = "output.txt";
        
        if(args.length > 0){
            inputFileName = args[0];

            if (args.length > 1){
                outputFileName = args[1];
            }
        }

        System.out.println("Loading json from file path : " + inputFileName);
        JSONObject inputJSONData = app.readJsonFromFile(inputFileName);
        System.out.println("Parsed json : " + inputJSONData.toString());
        System.out.println("");

        StudentInfo studentInfo = null;
        try{
            studentInfo = app.extractStudentInfo(inputJSONData);
        }catch(CredsNotFound e){
            System.out.println("The given json input did not have the necessary credentials. ");
            System.exit(1);
        }

        System.out.println();
        System.out.println("Student first name    : " + studentInfo.firstName);
        System.out.println("Student roll number   : " + studentInfo.rollNumber);

        String hash = app.generateMD5HashFromCredentials(studentInfo.firstName, studentInfo.rollNumber);
        System.out.println("The md5 hash is : " + hash);

        app.dumpStringToFile(outputFileName, hash);

    }
}
