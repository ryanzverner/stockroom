Sequel.migration do
  up do
    add_column :engagements, :confidence_percentage, Integer
  end

  down do
    drop_column :engagements, :confidence_percentage
  end
end