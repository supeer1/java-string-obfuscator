package me.supeer.obfuscator.basic.obf;
/*
 *
 * @Author supeer
 *
 *
 * - 2022 -
 *
 */


import me.supeer.obfuscator.basic.Main;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class Transformer implements Opcodes{

    public String na;
    public File input;
    public String main;

    public JLabel info;

    public boolean stringEncryptionEnabled;
    public boolean imageEncryptionEnabled;

    private HashMap<String, ClassNode> classes = new HashMap<>();
    private HashMap<String, JarEntry> files = new HashMap<>();
    private HashMap<String, byte[]> addingImages = new HashMap<>();

    public Transformer(File f, String na, JLabel i, boolean a, boolean b) {
        this.input = f;
        this.na = na;
        this.info = i;
        this.stringEncryptionEnabled = a;
        this.imageEncryptionEnabled = b;
    }
    public void transform(){
        String key = String.valueOf((char) new Random().nextInt(Integer.MAX_VALUE));
        ClassNode decryptClass = StringMethod.getDecryptClass(na, key);
        classes.put(decryptClass.name + ".class", decryptClass);
        for(ClassNode classNode : classes.values()){
            if((classNode.access & Opcodes.ACC_INTERFACE) != 0 || classNode.name.equals(decryptClass.name)){
                continue;
            }

            if(this.imageEncryptionEnabled){
                a:for(MethodNode methodNode : classNode.methods){
                    String splitKey = "31316969";
                    String name = name(String.valueOf((classNode.name + "." + methodNode.name + methodNode.desc + String.valueOf(methodNode.access)).hashCode()));
                    String full = "";
                    int index = 1;
                    for(AbstractInsnNode insnNode : methodNode.instructions){
                        if(insnNode instanceof LdcInsnNode){
                            LdcInsnNode ldcInsnNode = (LdcInsnNode) insnNode;
                            if(ldcInsnNode.cst instanceof String){
                                String value = (String) ldcInsnNode.cst;
                                try{
                                    full += splitKey;
                                    full += new String(Base64.getEncoder().encode(value.getBytes(StandardCharsets.UTF_8)));
                                }
                                catch (Exception ex){
                                    continue a;
                                }
                                InsnList block = new InsnList();
                                block.add(new LdcInsnNode("/" + name));
                                block.add(toInsnNode(index));
                                block.add(new LdcInsnNode(splitKey));
                                block.add(new MethodInsnNode(INVOKESTATIC, decryptClass.name, "get", "(Ljava/lang/String;ILjava/lang/String;)Ljava/lang/String;", false));

                                methodNode.instructions.insert(insnNode, block);
                                methodNode.instructions.remove(insnNode);
                                index++;
                            }
                        }
                    }
                    if(full.isEmpty()){
                        continue;
                    }
                    addingImages.put(name, full.getBytes(StandardCharsets.UTF_8));
                }
            }

            if(this.stringEncryptionEnabled){
                for(MethodNode methodNode : classNode.methods){
                    for(AbstractInsnNode insnNode : methodNode.instructions){
                        if(insnNode instanceof LdcInsnNode){
                            LdcInsnNode ldcInsnNode = (LdcInsnNode) insnNode;
                            if(!(ldcInsnNode.cst instanceof String)){
                                continue;
                            }
                            String value = (String) ldcInsnNode.cst;
                            if(!value.isEmpty()){
                                int rand = 0;
                                do{
                                    rand = new Random().nextInt(Integer.MAX_VALUE);
                                }while (rand == 0);

                                String encrypted = StringMethod.encrypt(value);
                                String encrypted2 = StringMethod.encrypt2(encrypted, rand);
                                InsnList block = new InsnList();
                                block.add(new TypeInsnNode(NEW, decryptClass.name));
                                block.add(new InsnNode(DUP));
                                block.add(new LdcInsnNode(encrypted2));
                                block.add(toInsnNode(rand));
                                block.add(new MethodInsnNode(INVOKESPECIAL, decryptClass.name, "<init>", "(Ljava/lang/String;I)V", false));
                                block.add(new MethodInsnNode(INVOKEVIRTUAL, decryptClass.name, "toString", "()Ljava/lang/String;", false));

                                methodNode.instructions.insert(insnNode, block);
                                methodNode.instructions.remove(insnNode);
                            }
                        }
                    }
                }
            }
        }
    }

    public static String name(String n){
        return "META-INF/DATA/" + n + (new Random().nextBoolean() ? ".png" : ".jpg");
    }

    public static AbstractInsnNode toInsnNode(int value){
        if(value > 5 || value < 0){
            LdcInsnNode ldcInsnNode = new LdcInsnNode(value);
            return ldcInsnNode;
        }
        switch (value){
            case 0:{
                return new InsnNode(ICONST_0);
            }
            case 1:{
                return new InsnNode(ICONST_1);
            }
            case 2:{
                return new InsnNode(ICONST_2);
            }
            case 3:{
                return new InsnNode(ICONST_3);
            }
            case 4:{
                return new InsnNode(ICONST_4);
            }
            case 5:{
                return new InsnNode(ICONST_5);
            }
            case -1:{
                return new InsnNode(ICONST_M1);
            }
        }
        return null;
    }

    public void load(){
        try{
            JarFile jarFile = new JarFile(input);
            if(jarFile.getManifest() != null){
                String main = jarFile.getManifest().getMainAttributes().getValue("Main-Class");
                this.main = main != null && !main.isEmpty() ? main : "";
            }
            Enumeration<? extends JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()){
                JarEntry entry = entries.nextElement();
                if(entry.getName().endsWith(".class")){
                    ClassReader classReader = new ClassReader(jarFile.getInputStream(entry));
                    ClassNode classNode = new ClassNode();
                    classReader.accept(classNode, 0);
                    classes.put(classNode.name + ".class", classNode);
                    continue;
                }
                this.files.put(entry.getName(), entry);
            }
        }
        catch (Exception ex){
            JOptionPane.showMessageDialog(null, ex.getMessage(), ex.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
        }
    }

    public void save(){
        String outputPath = input.getAbsolutePath() + "_"+System.currentTimeMillis() + ".jar";
        try{
            JarFile jarFile = new JarFile(input);
            File output = new File(outputPath);
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(output));

            for(JarEntry jarEntry : files.values()){
                jarOutputStream.putNextEntry(jarEntry);
                writeToFile(jarOutputStream, jarFile.getInputStream(jarEntry));
            }

            for(ClassNode classNode : classes.values()){
                try{
                    jarOutputStream.putNextEntry(new JarEntry(classNode.name + ".class"));
                    jarOutputStream.write(toByteArray(classNode));
                    jarOutputStream.closeEntry();
                }
                catch (Throwable e){
                    continue;
                }
            }

            for(String key : addingImages.keySet()){
                JarEntry entry = new JarEntry(key);
                byte[] bytes = addingImages.get(key);
                jarOutputStream.putNextEntry(entry);
                writeToFile(jarOutputStream, bytes);
            }


            jarOutputStream.setComment("Basic Obfuscator V " + Main.version);
            jarOutputStream.close();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        this.info.setText("Saved to " + outputPath);
    }

    public static byte[] toByteArray(ClassNode classNode){
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        return cw.toByteArray();
    }

    public static void writeToFile(JarOutputStream zipOutputStream, InputStream inputStream) {
        try {
            byte[] byArray = new byte[4096];
            try {
                while (inputStream.available() > 0) {
                    int n = inputStream.read(byArray);
                    zipOutputStream.write(byArray, 0, n);
                }
            }
            finally {
                inputStream.close();
                zipOutputStream.closeEntry();
            }
        }
        catch (Exception exception) {
        }
    }

    public static void writeToFile(JarOutputStream zipOutputStream, byte[] bytes) {
        try {
            try {
                zipOutputStream.write(bytes);
            }
            finally {
                zipOutputStream.closeEntry();
            }
        }
        catch (Exception exception) {
        }
    }
}
