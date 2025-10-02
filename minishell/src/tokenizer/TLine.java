package tokenizer;

import java.util.ArrayList;
import java.util.List;

// Clase para representar una línea de comandos completa (tline)
public class TLine {
    int ncommands;                   // Número de comandos en la línea
    List<TCommand> commands;         // Lista de comandos
    String redirectInput;            // Archivo de redirección de entrada

    public String getRedirectOutput() {
        return redirectOutput;
    }

    public void setRedirectOutput(String redirectOutput) {
        this.redirectOutput = redirectOutput;
    }

    public int getNcommands() {
        return ncommands;
    }

    public void setNcommands(int ncommands) {
        this.ncommands = ncommands;
    }

    public List<TCommand> getCommands() {
        return commands;
    }

    public void setCommands(List<TCommand> commands) {
        this.commands = commands;
    }

    public String getRedirectInput() {
        return redirectInput;
    }

    public void setRedirectInput(String redirectInput) {
        this.redirectInput = redirectInput;
    }

    public String getRedirectError() {
        return redirectError;
    }

    public void setRedirectError(String redirectError) {
        this.redirectError = redirectError;
    }

    public boolean isBackground() {
        return background;
    }

    public void setBackground(boolean background) {
        this.background = background;
    }

    public boolean isAppendOutput() { return appendOutput; }

    public void setAppendOutput(boolean appendOutput) { this.appendOutput = appendOutput; }

    public boolean isAppendError() { return appendError; }

    public void setAppendError(boolean appendError) { this.appendError = appendError; }

    String redirectOutput;           // Archivo de redirección de salida
    String redirectError;            // Archivo de redirección de errores
    boolean background;              // Indica si el proceso debe ejecutarse en segundo plano
    boolean appendOutput;            // Indica si la redirección de salida es en modo append
    boolean appendError;            // Indica si la redirección de salida es en modo append

    public TLine() {
        this.commands = new ArrayList<>();
        this.redirectInput = null;
        this.redirectOutput = null;
        this.redirectError = null;
        this.background = false;
        this.appendOutput = false;
        this.appendError = false;
    }

    public void addCommand(TCommand command) {
        commands.add(command);
        ncommands = commands.size();
    }

    @Override
    public String toString() {
        return "TLine{" +
                "ncommands=" + ncommands +
                ", commands=" + commands +
                ", redirectInput='" + redirectInput + '\'' +
                ", redirectOutput='" + redirectOutput + '\'' +
                ", redirectError='" + redirectError + '\'' +
                ", background=" + background +
                ", appendOutput=" + appendOutput +
                ", appendError=" + appendError +
                '}';
    }
}
