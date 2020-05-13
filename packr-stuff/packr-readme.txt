INSTRUCTIONS TO CREATE PACKR

1. Download packr.jar from packr github (on the readme)
2. Download Java 9.0.4 for the OS you want. Deal with the .tar.gz (I had to convert that to zip, and then unzip the zip)
3. Within the big folder, in the subfolder containing bin and lib (different for Mac vs Windows), create a folder called jre. Move "bin" and "lib" into jre. 
4. Zip up the "jdk-9.0.4.jdk" folder
5. Move the script you need, the jar you want to create an executable, the packr.jar, and the zipped jdk **into the folder containing the fallen-flame repository, but not the actual repository**
6. Update the jdk path with the local path containing the zipped jdk, if needed
7. In the terminal/command prompt, run "java -jar packr.jar packr-[os-name].json"
8. Profit