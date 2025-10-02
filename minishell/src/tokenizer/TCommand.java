package tokenizer;
import java.util.List;

public class TCommand {
    String filename;    // El nombre del archivo ejecutable o comando
    int argc; // Número de argumentos
    List<String> argv;  // Lista de argumentos

    public List<String> getArgv() {
        return argv;
    }

    public void setArgv(List<String> argv) {
        this.argv = argv;
    }

    public int getArgc() {
        return argc;
    }

    public void setArgc(int argc) {
        this.argc = argc;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public TCommand(String filename, List<String> argv) {
        this.filename = filename;
        this.argv = argv;
        this.argc = argv.size();
    }

    @Override
    public String toString() {
        return "Command: " + filename + ", Args: " + argv;
    }
}