package storagetest;

import gdstorage.GoogleDriveStorage;
import localstorage.LocalStorage;
import storagecore.StorageCore;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.file.FileAlreadyExistsException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        StorageCore storageCore = new LocalStorage("def");//Stavljeno kao default storage
        System.out.println("Welcome, to storage");
        System.out.println("To create local storage input 1,to create google drive storage input 2");
        String input = "";

        while (!input.equalsIgnoreCase("exit")) {
            Scanner s = new Scanner(new InputStreamReader(System.in));
            input = s.nextLine();
            //Exit
            if (input.equalsIgnoreCase("exit")) {
                continue;
            }

            //Creating storage
            if (input.equals("1") || input.equals("2")) {
                System.out.println("Enter root directory(enter def for default directory):");
                String directory = s.nextLine();

                System.out.println("Enter maximum size(in bytes)");
                int maxbytes = Integer.parseInt(s.nextLine());

                System.out.println("Enter all banned extensions:");
                List<String> bannedextensions = List.of(s.nextLine().split(" "));

                System.out.println("Enter file count limit:");
                int filecount = Integer.parseInt(s.nextLine());

                if (input.equals("1")) {
                    storageCore = new LocalStorage(directory, maxbytes, bannedextensions, filecount);
                } else {
                    storageCore = new GoogleDriveStorage(directory, maxbytes, bannedextensions, filecount);
                }
            }

            //Create directory
            else if (input.split(" ")[0].equalsIgnoreCase("create")) {

                List<String> niz = List.of(input.split(" "));
                if (niz.size() == 2)//Create imefajla
                {
                    try {
                        storageCore.createDirectory(niz.get(1));
                    } catch (FileAlreadyExistsException e) {
                        System.out.println("File already exists");
                        e.printStackTrace();
                    }
                } else if (niz.size() == 3)//Create 1 20
                {
                    try {
                        storageCore.createDirectory(Integer.parseInt(niz.get(1)), Integer.parseInt(niz.get(2)));
                    } catch (FileAlreadyExistsException e) {
                        System.out.println("File already exists");
                        e.printStackTrace();
                    }
                } else if (niz.size() == 4)//Create 1 20 imefajla
                {
                    try {
                        storageCore.createDirectory(niz.get(1), Integer.parseInt(niz.get(2)), Integer.parseInt(niz.get(1)));
                    } catch (FileAlreadyExistsException e) {
                        System.out.println("File already exists");
                        e.printStackTrace();
                    }
                }

            } else if (input.split(" ")[0].split(" ")[0].equalsIgnoreCase("addfile")) {
                List<String> niz = List.of(input.split(" "));
                if (niz.size() == 2) {
                    try {
                        storageCore.addFile(niz.get(1));
                    } catch (FileNotFoundException e) {
                        System.out.println("Invalid path");
                        e.printStackTrace();
                    } catch (FileAlreadyExistsException e) {
                        System.out.println("File already exists");
                        e.printStackTrace();
                    }
                }
            } else if (input.split(" ")[0].split(" ")[0].equalsIgnoreCase("delete")) {
                List<String> niz = List.of(input.split(" "));
                if (niz.size() == 2) {
                    try {
                        storageCore.deleteFileOrFolder(niz.get(1));
                    } catch (FileNotFoundException e) {
                        System.out.println("Invalid file name");
                        e.printStackTrace();
                    }
                }
            } else if (input.split(" ")[0].equalsIgnoreCase("move")) {
                List<String> niz = List.of(input.split(" "));
                if (niz.size() == 3) {
                    try {
                        storageCore.moveFileOrDirectory(niz.get(1), niz.get(2));
                    } catch (FileNotFoundException e) {
                        System.out.println("Invalid path");
                        e.printStackTrace();
                    } catch (FileAlreadyExistsException e) {
                        System.out.println("File already exists");
                        e.printStackTrace();
                    }
                }

            } else if (input.split(" ")[0].equalsIgnoreCase("download")) {
                List<String> niz = List.of(input.split(" "));
                if (niz.size() == 3) {
                    try {
                        storageCore.moveFileOrDirectory(niz.get(1), niz.get(2));
                    } catch (FileNotFoundException e) {
                        System.out.println("Invalid path");
                        e.printStackTrace();
                    } catch (FileAlreadyExistsException e) {
                        System.out.println("File already exists");
                        e.printStackTrace();
                    }
                }
            } else if (input.equalsIgnoreCase("rename")) {
                List<String> niz = List.of(input.split(" "));
                if (niz.size() == 3) {
                    try {
                        storageCore.moveFileOrDirectory(niz.get(1), niz.get(2));
                    } catch (FileNotFoundException e) {
                        System.out.println("Invalid path");
                        e.printStackTrace();
                    } catch (FileAlreadyExistsException e) {
                        System.out.println("File already exists");
                        e.printStackTrace();
                    }
                }
            } else if (input.split(" ")[0].equalsIgnoreCase("searchByName")) {
                List<String> niz = List.of(input.split(" "));
                if (niz.size() == 2) {
                    storageCore.searchByName(niz.get(1));
                }
            } else if (input.split(" ")[0].equalsIgnoreCase("searchByExtension")) {
                List<String> niz = List.of(input.split(" "));
                if (niz.size() == 2) {
                    storageCore.searchByExtension(niz.get(1));
                }
            } else if (input.split(" ")[0].equalsIgnoreCase("searchByModifiedAfter")) {
                List<String> niz = List.of(input.split(" "));
                if (niz.size() == 2) {
                    try {
                        storageCore.searchByModifiedAfter(new SimpleDateFormat("dd/MM/yyyy").parse(niz.get(1)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            } else if (input.equalsIgnoreCase("help")) {
                System.out.println("Spisak komandi kako se koristi apk");
            }
        }
    }
}