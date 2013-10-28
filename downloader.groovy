import java.util.zip.ZipInputStream


def file = new File("addons.txt")

if(!file.exists()) {
    println "Please create an 'addons.txt' file in the current directory and try again!"
    System.exit(-1)
}

def addonsFolder = "/tmp/WoW"

file.eachLine { id ->

    if(id.trim().isEmpty()) {
        return
    }

    if(id.trim().isEmpty() || !id.startsWith("http://www.wowace.com/addons")) {
        println "Skipping invalid addon declaration: " + id
        return
    }

    println "Updating Project " + id
    def page = id.toURL().text

    page.replaceAll("<a href=\"(.*)\">Download</a>", {
        def downloadURL = "http://www.curseforge.com/" + it[1]
        def downloadPage = downloadURL.toURL().text

        downloadPage.replaceAll("<a href=\"(.*)\">Download</a>", {
            println "Downloading from " + it[1]

            def tmpFile = "/tmp/TEMP_DOWNLOAD"

            new File(tmpFile).delete()

            download(tmpFile, it[1])

            extract(tmpFile, addonsFolder)
        })
    })
}

def download(id, address) {
    def file = new FileOutputStream(id)
    def out = new BufferedOutputStream(file)
    out << new URL(address).openStream()
    out.close()
}

def extract(zipFile, outFile) {
    def result = new ZipInputStream(new FileInputStream(zipFile))
    def destFile = new File(outFile)
    if(!destFile.exists()){
        destFile.mkdir();
    }
    result.withStream{
        def entry
        while(entry = result.nextEntry){
            if (!entry.isDirectory()){
                new File(outFile + File.separator + entry.name).parentFile?.mkdirs()
                def output = new FileOutputStream(outFile + File.separator
                        + entry.name)
                output.withStream{
                    int len = 0;
                    byte[] buffer = new byte[4096]
                    while ((len = result.read(buffer)) > 0){
                        output.write(buffer, 0, len);
                    }
                }
            }
            else {
                new File(outFile + File.separator + entry.name).mkdir()
            }
        }
    }
}