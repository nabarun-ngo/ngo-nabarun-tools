package ngo.nabarun.tools.misc_tools.converter;

import java.io.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import ngo.nabarun.tools.misc_tools.converter.service.OpenAPIToExcelConverter;

@ShellComponent
@ShellCommandGroup("Converter")
public class ConvertCommands {

	@Autowired
	private OpenAPIToExcelConverter converterService;

	@ShellMethod(key = { "openapi2excel" })
	public void SyncAuth0Tenants(@ShellOption(value={ "-i", "--input" },defaultValue = "files/openapi.json") String input,
			@ShellOption(value = { "-o", "--output" },defaultValue = "files/OpenApiExcel.xlsx") String output

	) {
		converterService.convertToExcelV3(new File(input), new File(output));
	}

}
