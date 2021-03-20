package com.mndk.kvm2m.mod.commands;

import java.io.File;
import java.io.FileNotFoundException;

import org.kabeja.parser.ParseException;

import com.mndk.kvm2m.core.vectormap.VectorMapParserResult;
import com.mndk.kvm2m.core.vectorparser.DxfMapParser;

import net.minecraft.command.CommandException;

public class DxfMapGeneratorCommand extends VectorMapGeneratorCommand {


	@Override
	public String getName() {
		return "gendxfmap";
	}


	@Override
	public VectorMapParserResult fileDataToParserResult(File file) throws CommandException {
		try {
            return DxfMapParser.parse(file);
        } catch(ParseException exception) {
            throw new CommandException("There was an error while parsing .dxf map.");
        } catch(FileNotFoundException exception) {
        	throw new CommandException("File does not exist!");
        }
	}


	@Override
	public String getExtension() {
		return "dxf";
	}
	
	
}
