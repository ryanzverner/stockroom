Sequel.migration do
  up do
    from(:engagements)
      .where(:confidence_percentage => nil)
      .update(:confidence_percentage => 100)
  end
end
