package obfuscate.comand.autocomplete;

import obfuscate.comand.ExecutionContext;

import java.util.List;

public interface CommandAutocomplete {
    List<String> autocomplete(ExecutionContext context);
}
