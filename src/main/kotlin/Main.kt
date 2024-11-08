import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.system.exitProcess

val MAPPINGS = mapOf(
    "textures/blocks" to "textures/block",
    "textures/entity/endercrystal" to "textures/entity/end_crystal",
    "textures/items" to "textures/item",
    "apple_golden.png" to "golden_apple.png",
    "totem.png" to "totem_of_undying.png"
)

val DIAMOND_TO_NETHERITE = "diamond_" to "netherite_"

fun main(args: Array<String>) {
    val timestamp = System.currentTimeMillis()

    fun exit(message: String) {
        println("$message\nUsage: <path to 1.12.2 zipped pack> <path to 1.20.1 output directory> <true/false: using diamond textures as netherite textures>")
        exitProcess(0)
    }

    if (args.size != 3) {
        exit("Not enough arguments!")
    }

    val inputName = args[0]
    val outputDirName = args[1]
    val diamondToNetherite = args[2].toBoolean()

    val inputFile = File(inputName)
    val outputDir = File(outputDirName)

    if (!inputFile.exists() || !inputFile.isFile) {
        exit("Input file does not exist or is not a valid file!")
    }

    if (outputDir.exists()) {
        println("Output directory will be overwritten")
        outputDir.deleteRecursively()
    }

    outputDir.mkdirs() // Create the output directory

    var counter = 0

    fun mapName(name: String): String {
        var mappedName = name

        // Exclude models directory from mapping
        if (name.startsWith("assets/minecraft/models/")) {
            return name // Skip JSON model files
        }

        // Perform mappings based on the defined mappings
        for ((old, new) in MAPPINGS) {
            mappedName = mappedName.replace(old, new)
        }

        // Handle diamond to netherite texture mapping
        if (diamondToNetherite) {
            mappedName = mappedName.replace(DIAMOND_TO_NETHERITE.first, DIAMOND_TO_NETHERITE.second)
        }

        // Count how many names have changed
        if (name != mappedName) {
            counter++
        }

        return mappedName
    }

    try {
        val inputZipFile = ZipFile(inputFile)
        val zos = ZipOutputStream(FileOutputStream(File(outputDir, "output.zip"))) // Create a zip file in the output directory

        for (inputEntry in inputZipFile.entries()) {
            val name = inputEntry.name
            val mappedName = mapName(name)

            // Create the output entry
            val outputEntry = ZipEntry(mappedName)

            // Ensure the output entry's parent directories exist
            val parentDir = File(mappedName).parent
            if (parentDir != null) {
                zos.putNextEntry(ZipEntry("$parentDir/")) // Ensure parent directories are created
            }

            zos.putNextEntry(outputEntry)

            inputZipFile.getInputStream(inputEntry).use { inputStream ->
                inputStream.copyTo(zos)
            }

            zos.closeEntry()
        }

        zos.close()

        println("Mapped $counter zip entries! Everything took ${System.currentTimeMillis() - timestamp} ms!")
    } catch (e: Exception) {
        println("An error occurred: ${e.message}")
        exitProcess(1)
    }
}
