package storagetest;

import gdstorage.GoogleDriveStorage;
import localstorage.LocalStorage;
import storagecore.StorageCore;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {



        System.out.println("Welcome, to storage");
        System.out.println("To create local storage input 1,to create google drive storage input 2");
        String input="";

        while (!input.equalsIgnoreCase("exit")) {
            Scanner s = new Scanner(new InputStreamReader(System.in));
            input = s.nextLine();


            //Exit
            if(input.equalsIgnoreCase("exit")) {
                continue;
            }

            //Creating storage
            if(input.equals("1") || input.equals("2"))
            {
                System.out.println("Enter root directory(enter def for default directory):");//                         Optional
                String directory = s.nextLine();

                System.out.println("Enter maximum size(in bytes)");//                                                   Optional
                int maxbytes = Integer.parseInt(s.nextLine());


                System.out.println("Enter all banned extensions:");//                                                   Optional
                List<String> bannedextensions = List.of(s.nextLine().split(" "));


                System.out.println("Enter file count limit:");//                                                        Mandatory
                int filecount = Integer.parseInt(s.nextLine());

                if(input.equals("1"))
                {
                    StorageCore storageCore =new LocalStorage(directory,maxbytes,bannedextensions,filecount);
                }
                else
                {
                    StorageCore storageCore =new GoogleDriveStorage(directory,maxbytes,bannedextensions,filecount);
                }
            }

            else if(input.equalsIgnoreCase("help"))
            {
                System.out.println("Spisak komandi kako se koristi apk");
            }


        }

    }


}