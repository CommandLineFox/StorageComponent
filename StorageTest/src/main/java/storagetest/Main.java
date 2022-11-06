package storagetest;

import storagecore.StorageCore;
import storagecore.StorageManager;

import java.util.*;

public class Main {
    public static StorageCore createStorage(String[] args) throws Exception {
        String storageType = args[0];
        String path = args[1];
        String type;
        if (storageType.equalsIgnoreCase("google")) {
            type = "gdstorage.GoogleDriveStorage";
        } else if (storageType.equalsIgnoreCase("local")) {
            type = "localstorage.LocalStorage";
        } else {
            type = "invalid";
        }

        if (type.equals("invalid")) {
            throw new Exception("Invalid storage type provided");
        }

        Class.forName(type);
        return StorageManager.getStorage(path);
    }

    public static StorageCore checkRoot(StorageCore storage, Scanner input) throws Exception {
        boolean validRoot = storage.checkRoot(storage.getRoot());
        if (validRoot) {
            return storage;
        }

        System.out.println("Provided root doesn't exist");
        if (!storage.createRoot(storage.getRoot())) {
            throw new Exception("Couldn't create the root, please try again with a different root");
        }

        System.out.println("Created the missing root successfully");
        return storage;
    }

    public static StorageCore checkConfig(StorageCore storage, Scanner input) {
        boolean configExists = storage.checkConfig(storage.getRoot());
        if (configExists) {
            System.out.println("Configuration already exists, starting the instance");
            return storage;
        }

        System.out.println("Configuration not found, would you like to create a new one or use a default one");
        System.out.println("1 - Create a new one");
        System.out.println("2 - Use a default one");
        String option = input.nextLine().trim();

        boolean task = false;
        while (!task) {
            switch (option) {
                case "1" -> {
                    System.out.println("Enter the max size the storage would be");
                    double maxSizeLimit = input.nextDouble();

                    System.out.println("Enter a list of banned extensions separated by commas");
                    String[] extensions = input.nextLine().split(",");
                    List<String> bannedExtensions = new ArrayList<>();
                    for (String extension : extensions) {
                        bannedExtensions.add(extension.toLowerCase().trim());
                    }

                    storage.createConfig(maxSizeLimit, bannedExtensions);
                    System.out.println("Created a new configuration");
                    task = true;
                }
                case "2" -> {
                    storage.createConfig();
                    System.out.println("Created a default configuration");
                    task = true;
                }
                default -> System.out.println("invalid option, valid options are 1 and 2");
            }
        }

        return storage;
    }

    public static String getCommand(String input) {
        String[] split = input.split("/\s/");
        if (split.length == 0) {
            System.out.println("Couldn't find a command");
            return null;
        }
        return split[0].trim().toLowerCase();
    }

    public static String[] getArguments(String input) {
        String[] split = input.split("/\s/");
        List<String> arguments = new ArrayList<>(Arrays.asList(split).subList(1, split.length));
        String[] args = new String[arguments.size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = arguments.get(i);
        }

        return args;
    }

    public static void handleCommands(StorageCore storage, Scanner input) {
        String line = input.nextLine();
        String command = getCommand(line);
        while (!Objects.equals(command, "exit")) {
            if (command == null) {
                line = input.nextLine();
                command = getCommand(line);
                continue;
            }

            switch (command) {
                case "cd" -> {
                    String[] args = getArguments(line);
                    if (args.length != 1) {
                        System.out.println("Expected 1 argument, got 0");
                        break;
                    }

                    String path = args[0];
                    boolean success = false;
                    if (path.equalsIgnoreCase("..")) {
                        success = storage.returnBackFromDirectory();

                        if (success) {
                            System.out.println("Successfully returned to parent directory");
                        } else {
                            System.out.println("Unable to return to parent directory");
                        }
                    } else {
                        success = storage.enterDirectory(path);
                        if (success) {
                            System.out.println("Successfully entered new directory as root");
                        } else {
                            System.out.println("Unalbe to enter new directory");
                        }
                    }
                }
                case "create" -> {
                    System.out.println("create");
                }
                default -> System.out.println("Invalid command, please see \"help\" for a list of valid commands");
            }

            line = input.nextLine();
            command = getCommand(line);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2 || args.length > 2) {
            throw new Exception("Invalid amount of arguments, expected 2, got " + args.length);
        }

        System.out.println("Welcome to storage");

        Scanner input = new Scanner(System.in);
        StorageCore storage = createStorage(args);
        ;
        storage = checkRoot(storage, input);
        storage = checkConfig(storage, input);

        /*System.out.println("Type a command or \"exit\" to close the program. See \"help\" for a full list of commands");
        handleCommands(storage, input);

        StorageCore storageCore = null;
        System.out.println("Welcome, to storage");
        System.out.println("To create local storage input 1,to create google drive storage input 2");
        String input = "";

        Scanner s = new Scanner(new InputStreamReader(System.in));
        input = s.nextLine();
        //Creating storage
        if (input.equals("1") || input.equals("2")) {
            System.out.println("Enter root directory(enter def for default directory):");
            String directory = s.nextLine();

            System.out.println("Enter maximum size(in bytes)");
            int maxbytes = Integer.parseInt(s.nextLine());

            System.out.println("Enter all banned extensions:");
            List<String> bannedextensions = List.of(s.nextLine().split(" "));


            if (input.equals("1")) {


                //storageCore = new LocalStorage(directory, maxbytes, bannedextensions, filecount);
            } else {
                if (directory.equalsIgnoreCase("def"))
                    directory = "Root";

                storageCore = new GoogleDriveStorage(directory, maxbytes, bannedextensions);

            }
        }
        if (storageCore != null)
            while (!input.equalsIgnoreCase("exit")) {
                input = s.nextLine();
                //Exit
                if (input.equalsIgnoreCase("exit")) {
                    break;
                }

                if (input.split(" ")[0].equalsIgnoreCase("cd")) {
                    List<String> niz = List.of(input.split(" "));
                    if (niz.size() == 2)//Create imefajla
                    {
                        if (niz.get(1).equals(".."))//VRATI SE NAZAD
                        {
                            storageCore.returnBackFromDirectory();

                        } else//Idi u zadati direktorijum
                        {
                            storageCore.enterDirectory(niz.get(1));
                        }
                    }
                } else if (input.split(" ")[0].equalsIgnoreCase("create")) {

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
                        } catch (FileAlreadyExistsException e) {
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
            }*/
    }
}